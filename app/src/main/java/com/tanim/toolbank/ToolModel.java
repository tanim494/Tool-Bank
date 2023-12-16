package com.tanim.toolbank;

public class ToolModel {
    private String name;
    private int iconResourceId;

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