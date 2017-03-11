package com.justadeveloper96.permissionmanager;


/**
 * Created by harshit on 10-03-2017.
 */

public class PermissionModel {

    private String name;
    private String value;
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public PermissionModel(String permission) {
        value = permission;
        String[] split=permission.split("\\.");
        name=split[split.length-1];
        selected=false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return name;
    }
}
