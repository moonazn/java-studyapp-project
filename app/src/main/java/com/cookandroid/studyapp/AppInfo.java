package com.cookandroid.studyapp;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String packageName;
    private String name;
    private Drawable icon;
    private boolean selected;

    public AppInfo(String packageName, String name, Drawable icon) {
        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
        this.selected = false; // 초기에는 선택되지 않은 것으로 설정
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setUsabled(boolean selected) {
        this.selected = selected;
    }
}
