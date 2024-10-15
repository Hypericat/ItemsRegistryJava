import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import items.Item;
import items.component.Component;
import items.component.Rarity;
import items.component.Tool;
import items.component.ToolRule;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        FileReader file;
        if (args.length > 0) {
            file = new FileReader(args[0]);
        } else {
            file = new FileReader("items.json");
        }
        List<String> javas = parseJson(file);


        File out = new File("out.txt");
        if (out.exists()) out.delete();
        out.createNewFile();
        FileWriter writer = new FileWriter(out);
        for (String s : javas) {
            writer.write(s + "\n");
            writer.flush();
            System.out.println(s);
        }
        System.out.println("Saved to out.txt");
    }

    public static List<String> parseJson(FileReader file) {
        List<Pair<String, String>> packetTypes = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(file, JsonObject.class);
        List<String> done = new ArrayList<>();

        // Loop through each item (like "minecraft:glass_pane")
        for (Map.Entry<String, JsonElement> itemEntry : jsonObject.entrySet()) {
            String itemName = itemEntry.getKey().replace("minecraft:", ""); // e.g. "minecraft:glass_pane"
            JsonObject itemObject = itemEntry.getValue().getAsJsonObject();

            System.out.println("ItemName: " + itemName);
            Component.builder builder = Component.getBuilder();
            JsonObject components = itemObject.getAsJsonObject("components");

            for (Map.Entry<String, JsonElement> componentEntry : components.entrySet()) {
                String componentName = componentEntry.getKey();  // e.g. "minecraft:attribute_modifiers"
                JsonElement componentValue = componentEntry.getValue();  // The actual component value (which can be anything)
                parseObject(componentValue, packetTypes, componentName, builder);

                System.out.println("  Component: " + componentName);
            }

            Component component = builder.build();
            done.add(genJava(new Item(itemName, component)));
        }
        return done;
    }
    //public static Item IRON_SWORD = register(new Item("iron_sword", Component.getBuilder().setDamage(0).setMaxDamage(250).setMaxStackSize(1).setRarity(Rarity.COMMON).setRepairCost(0).setTool(new Tool(new ToolRule(List.of("minecraft:cobweb"), true, 15d), new ToolRule(List.of("#minecraft:sword_efficient"), false, 1.5d)).setDamagePerBlock(2)).build()));

    public static String genJava(Item item) {
        StringBuilder builder = new StringBuilder();
        builder.append("public static Item ");
        builder.append(item.getIdentifier().toUpperCase());
        builder.append(" = register(new Item(\"");
        builder.append(item.getIdentifier());
        builder.append("\", Component.getBuilder()");
        if (item.getComponent().getDamage() != 0)
            builder.append(".setDamage(" + item.getComponent().getDamage() + ")");
        if (item.getComponent().getMaxDamage() != -1)
            builder.append(".setMaxDamage(" + item.getComponent().getMaxDamage() + ")");
        if (item.getComponent().getMaxStackSize() != 64)
            builder.append(".setMaxStackSize(" + item.getComponent().getMaxStackSize() + ")");
        if (item.getComponent().getRarity() != Rarity.COMMON)
            builder.append(".setRarity(Rarity." + item.getComponent().getRarity().name() + ")");
        if (item.getComponent().getRepairCost() != 0)
            builder.append(".setRepairCost(" + item.getComponent().getRepairCost() + ")");

        if (item.getComponent().getMapColor() != -1) {
            builder.append(".setMapColor(" + item.getComponent().getMapColor() + ")");
        }

        if (item.getComponent().getOminousBottleAmplifier() != -1) {
            builder.append(".setOminousBottleAmplifier(" + item.getComponent().getOminousBottleAmplifier() + ")");
        }

        if (item.getComponent().isEnchantmentGlintOverride())
            builder.append(".setEnchantmentGlintOverride(" + item.getComponent().isEnchantmentGlintOverride() + ")");

        if (item.getComponent().getTool() != null && item.getComponent().getTool().getRules().size() > 1) {
            builder.append(".setTool(new Tool(");
            for (ToolRule toolRule : item.getComponent().getTool().getRules()) {
                builder.append("new ToolRule(List.of(");
                for (String s : toolRule.getAffectedBlocks()) {
                    builder.append("\"" + s + "\"");
                    builder.append(", ");
                }
                if (builder.toString().endsWith(", ")) builder.delete(builder.length() - 2, builder.length());
                builder.append("), ");
                builder.append(toolRule.isCorrectForDrops());
                builder.append(", ");
                builder.append(toolRule.getSpeed() + "d");
                builder.append(")");
                builder.append(", ");
            }
            if (builder.toString().endsWith(", ")) builder.delete(builder.length() - 2, builder.length());
            builder.append(")");
            builder.append(".setDamagePerBlock(" + item.getComponent().getTool().getDamagePerBlock() + "))");
        }
        builder.append(".build()));");


        return builder.toString();
    }

    public static void parseObject(JsonElement jsonElement, List<Pair<String, String>> packetTypes, String componentName, Component.builder builder) {
        if (jsonElement.isJsonObject()) {
            JsonObject componentObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> componentEntry : componentObject.entrySet()) {
                String name = componentEntry.getKey();
                JsonElement componentValue = componentEntry.getValue();
                parseObject(componentValue, packetTypes, name, builder);
            }
        } else if (jsonElement.isJsonArray()) {
            parseArray(jsonElement.getAsJsonArray(), componentName, builder, packetTypes);
        } else if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            parsePrimitive(primitive, componentName, builder, packetTypes);
        }
    }


    public static void parseArray(JsonArray array, String componentName, Component.builder builder, List<Pair<String, String>> packetTypes) {
        //System.out.println("Array name : " + componentName + " value " + primitive.getAsString());
        switch (componentName) {
            case "rules" -> {
                Tool tool = builder.getTool();
                if (tool == null) tool = new Tool();
                for (JsonElement element : array) {
                    List<String> blocks = new ArrayList<>();
                    boolean drops = false;
                    double speed = 2d;
                    for (Map.Entry<String, JsonElement> componentEntry : element.getAsJsonObject().entrySet()) {
                        String name = componentEntry.getKey();
                        JsonElement componentValue = componentEntry.getValue();
                        if (componentValue.isJsonPrimitive()) {
                            JsonPrimitive primitive = componentValue.getAsJsonPrimitive();
                            if (name.equals("blocks")) {
                                blocks.add(primitive.getAsString());
                                continue;
                            }
                            if (name.equals("correct_for_drops")) {
                                drops = primitive.getAsBoolean();
                                continue;
                            }
                            speed = primitive.getAsDouble();
                            continue;
                        }
                        JsonArray array1 = componentValue.getAsJsonArray();
                        for (JsonElement element2 : array1) {
                            blocks.add(element2.getAsJsonPrimitive().getAsString());
                        }
                    }
                    tool.addRule(new ToolRule(blocks, drops, speed));
                    builder.setTool(tool);
                }
            }

        }
    }

    public static void parsePrimitive(JsonPrimitive primitive, String componentName, Component.builder builder, List<Pair<String, String>> packetTypes) {
        componentName = componentName.replace("minecraft:", "");
        //damage per block //done
        //blocks
        //speed


        //type for modifiers
        //amount for modifiers
        //operation for modifiers
        //slot for modifiers

        String finalComponentName = componentName;
        switch (componentName) {
            case "max_stack_size" -> builder.setMaxStackSize(primitive.getAsInt());
            case "repair_cost" -> builder.setRepairCost(primitive.getAsInt());
            case "damage" -> builder.setDamage(primitive.getAsInt());
            case "max_damage" -> builder.setMaxDamage(primitive.getAsInt());
            case "map_color" -> builder.setMapColor(primitive.getAsInt());
            case "ominous_bottle_amplifier" -> builder.setOminousBottleAmplifier(primitive.getAsInt());
            case "enchantment_glint_override" -> builder.setEnchantmentGlintOverride(primitive.getAsBoolean());
            case "damage_per_block" -> {
                Tool tool = builder.getTool();
                if (tool == null) tool = new Tool();
                tool.setDamagePerBlock(primitive.getAsInt());
                builder.setTool(tool);
            }
            case "rarity" -> {
                for (Rarity r : Rarity.values()) {
                    if (r.toString().equals(primitive.getAsString())) {
                        builder.setRarity(r);
                        break;
                    }
                }
            }
            default -> {
                if (packetTypes.stream().noneMatch(stringStringPair -> stringStringPair.getLeft().equals(finalComponentName))) {
                packetTypes.add(new Pair<>(componentName, primitive.getAsString()));
                System.out.println("Unused primitive name : " + componentName);
                }
            }

        }
    }

    public static String toHex(int i) {
        return String.format("0x%02X", i);
    }
}