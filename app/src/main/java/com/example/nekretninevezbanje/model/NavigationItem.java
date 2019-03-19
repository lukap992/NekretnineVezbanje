package com.example.nekretninevezbanje.model;

import android.os.Parcel;
import android.os.Parcelable;

public class NavigationItem implements Parcelable {

    private String title;
    private String subTitle;
    private int icon;

    public NavigationItem(String title, String subTitle, int icon) {
        this.title = title;
        this.subTitle = subTitle;
        this.icon = icon;
    }

    protected NavigationItem(Parcel in) {
        title = in.readString();
        subTitle = in.readString();
        icon = in.readInt();
    }

    public static final Creator<NavigationItem> CREATOR = new Creator<NavigationItem>() {
        @Override
        public NavigationItem createFromParcel(Parcel in) {
            return new NavigationItem(in);
        }

        @Override
        public NavigationItem[] newArray(int size) {
            return new NavigationItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(subTitle);
        dest.writeInt(icon);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
