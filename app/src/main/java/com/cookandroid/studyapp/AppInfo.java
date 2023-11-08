package com.cookandroid.studyapp;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable {
    private String packageName;
    private String name;
    private Drawable icon;
    private boolean selected;
    private int appSelectImage; // 이미지 리소스 ID를 저장할 필드

    private boolean isLocked; // 잠긴 상태 여부를 나타내는 변수

    public AppInfo(String packageName, String name, Drawable icon, int appSelectImage) {
        // 생성자에 이미지 리소스 ID를 추가하여 초기화
        this.packageName = packageName;
        this.name = name;
        this.icon = icon;
        this.selected = false;
        this.appSelectImage = appSelectImage;
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

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getAppSelectImage() {
        return appSelectImage;
    }

    public void setAppSelectImage(int appSelectImage) {
        this.appSelectImage = appSelectImage;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    // Parcelable 인터페이스 구현
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // 객체의 멤버 변수를 Parcel에 쓰는 로직을 구현
        dest.writeString(packageName);
        dest.writeString(name);
        // 다른 멤버 변수들에 대해서도 동일한 방식으로 작성
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

    protected AppInfo(Parcel in) {
        packageName = in.readString();
        name = in.readString();
        // 다른 멤버 변수들에 대해서도 동일한 방식으로 읽어옴
    }
}
