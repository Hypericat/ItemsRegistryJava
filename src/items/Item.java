package items;

import items.component.Component;

public class Item {
    private final String identifier;
    private final Component component;

    public Item(String identifier, Component component) {
        this.identifier = identifier;
        this.component = component;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Component getComponent() {
        return component;
    }
}
