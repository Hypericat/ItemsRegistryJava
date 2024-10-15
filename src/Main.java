import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import items.component.Component;

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
        List<Pair<String, String>> packetTypes = parseJson(file);
        System.out.println(packetTypes);
        System.out.println("Size : " + packetTypes.size());


        if (true) return;
        //File out = new File("out.txt");
        //if (out.exists()) out.delete();
        //out.createNewFile();
        //FileWriter writer = new FileWriter(out);
        //for (String s : packetTypes) {
        //    writer.write(s + "\n");
        //    writer.flush();
        //    System.out.println(s);
        //}
        //System.out.println("Saved to out.txt");
    }

    public static List<Pair<String, String>> parseJson(FileReader file) {
        List<Pair<String, String>> packetTypes = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(file, JsonObject.class);

        // Loop through each item (like "minecraft:glass_pane")
        for (Map.Entry<String, JsonElement> itemEntry : jsonObject.entrySet()) {
            String itemName = itemEntry.getKey(); // e.g. "minecraft:glass_pane"
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
        }
        return packetTypes;
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
            parseArray(jsonElement.getAsJsonArray(), componentName, builder);
        } else if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            parsePrimitive(primitive, componentName, builder);
        }
    }


    public static void parseArray(JsonArray array, String componentName, Component.builder builder) {

    }

    public static void parsePrimitive(JsonPrimitive primitive, String componentName, Component.builder builder) {

    }

    public static String toHex(int i) {
        return String.format("0x%02X", i);
    }
}