package items.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tool {
    private final List<ToolRule> rules;
    private int damagePerBlock = 2;


    public Tool(ToolRule... rules) {
        this.rules = new ArrayList<>();
        this.rules.addAll(Arrays.stream(rules).toList());
    }

    public int getDamagePerBlock() {
        return damagePerBlock;
    }

    public Tool setDamagePerBlock(int damagePerBlock) {
        this.damagePerBlock = damagePerBlock;
        return this;
    }

    public List<ToolRule> getRules() {
        return rules;
    }

    public void addRule(ToolRule rule) {
        rules.add(rule);
    }
}
