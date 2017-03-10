package com.justadeveloper96.permissionmanager;

import android.databinding.ObservableField;

/**
 * Created by harshit on 10-03-2017.
 */

public class PermissionModel {

    private String name;
    private String value;
    private boolean selected=false;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public PermissionModel(String name, String value) {
        this.name = name;
        this.value = value;
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
