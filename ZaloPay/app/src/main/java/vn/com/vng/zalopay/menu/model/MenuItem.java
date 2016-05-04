package vn.com.vng.zalopay.menu.model;

import android.os.Parcel;
import android.os.Parcelable;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 04/05/2016.
 */
public class MenuItem extends AbstractData {

    private int id;
    private MenuItemType itemType;
    private int resource;
    private String title;

    public MenuItem(int id, MenuItemType itemType, int resource, String title) {
        this.id = id;
        this.itemType = itemType;
        this.resource = resource;
        this.title = title;
    }

    public MenuItem(Parcel source) {
        id = source.readInt();
        itemType = (MenuItemType) source.readSerializable();
        resource = source.readInt();
        title = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeSerializable(itemType);
        dest.writeInt(resource);
        dest.writeString(title);
    }

    public static final Parcelable.Creator<MenuItem> CREATOR = new Parcelable.Creator<MenuItem>() {
        @Override
        public MenuItem createFromParcel(Parcel source) {
            return new MenuItem(source);
        }

        @Override
        public MenuItem[] newArray(int size) {
            return new MenuItem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (o instanceof MenuItem) {
            return id == ((MenuItem) o).id && id >= 0;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public MenuItemType getItemType() {
        return itemType;
    }

    public int getResource() {
        return resource;
    }

    public String getTitle() {
        return title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setItemType(MenuItemType itemType) {
        this.itemType = itemType;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
