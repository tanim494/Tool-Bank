package com.tanim.toolbank;

public class ToolModel {
    private final String name;
    private final int iconResourceId;

    public ToolModel(String name, int iconResourceId) {
        this.name = name;
        this.iconResourceId = iconResourceId;
    }

    public String getName() {
        return name;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }
}