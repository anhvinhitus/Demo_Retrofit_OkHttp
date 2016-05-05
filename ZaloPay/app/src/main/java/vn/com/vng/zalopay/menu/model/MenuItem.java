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
    private Integer iconResource;
    private String title;
    private Integer subIconResource;
    private boolean showDivider;

    public MenuItem(int id, MenuItemType itemType, Integer iconResource, Integer subIconResource, String title, boolean showDivider) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.iconResource = iconResource;
        this.subIconResource = subIconResource;
        this.showDivider = showDivider;
    }

    public MenuItem(int id, MenuItemType itemType, Integer iconResource, Integer iconSubResource, String title) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.iconResource = iconResource;
        this.subIconResource = iconSubResource;
        this.showDivider = true;
    }

    public MenuItem(int id, MenuItemType itemType, Integer iconResource,String title) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.iconResource = iconResource;
        this.subIconResource = null;
        this.showDivider = true;
    }

    public MenuItem(Parcel source) {
        id = source.readInt();
        itemType = (MenuItemType) source.readSerializable();
        title = source.readString();
        iconResource = source.readInt();
        subIconResource = source.readInt();
        showDivider = source.readInt()==1?true:false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeSerializable(itemType);
        dest.writeString(title);
        dest.writeInt(iconResource);
        dest.writeInt(subIconResource);
        dest.writeInt(showDivider?1:0);
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

    public Integer getIconResource() {
        return iconResource;
    }

    public String getTitle() {
        return title;
    }

    public Integer getSubIconResource() {
        return subIconResource;
    }

    public boolean isShowDivider() {
        return showDivider;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setItemType(MenuItemType itemType) {
        this.itemType = itemType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIconResource(Integer iconResource) {
        this.iconResource = iconResource;
    }

    public void setSubIconResource(Integer subIconResource) {
        this.subIconResource = subIconResource;
    }

    public void setShowDivider(boolean showDivider) {
        this.showDivider = showDivider;
    }
}
