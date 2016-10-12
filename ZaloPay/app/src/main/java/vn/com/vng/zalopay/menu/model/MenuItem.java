package vn.com.vng.zalopay.menu.model;

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

    public MenuItem(int id) {
        this.id = id;
    }

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

    public MenuItem(int id, MenuItemType itemType, Integer iconResource, String title, boolean showDivider) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.iconResource = iconResource;
        this.subIconResource = null;
        this.showDivider = showDivider;
    }

    public MenuItem(int id, MenuItemType itemType, Integer iconResource, String title) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.iconResource = iconResource;
        this.subIconResource = null;
        this.showDivider = true;
    }

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

    public void setTitle(String title) {
        this.title = title;
    }
}
