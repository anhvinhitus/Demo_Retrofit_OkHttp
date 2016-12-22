package vn.com.vng.zalopay.menu.model;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 04/05/2016.
 * *
 */
public class MenuItem extends AbstractData {

    private int id;
    private MenuItemType itemType;
    private Integer iconResource;
    private Integer iconColor;
    private String title;
    private boolean showDivider;

    public MenuItem(int id) {
        this.id = id;
    }

    public MenuItem(int id,
                    MenuItemType itemType,
                    String title) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.iconResource = null;
        this.iconColor = null;
    }

    public MenuItem(int id,
                    MenuItemType itemType,
                    String title,
                    Integer iconResource,
                    Integer iconColor,
                    boolean showDivider) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.iconResource = iconResource;
        this.iconColor = iconColor;
        this.showDivider = showDivider;
    }

    public MenuItem(int id,
                    MenuItemType itemType,
                    String title,
                    Integer iconResource,
                    Integer iconColor) {
        this.id = id;
        this.itemType = itemType;
        this.title = title;
        this.iconResource = iconResource;
        this.iconColor = iconColor;
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

    public boolean isShowDivider() {
        return showDivider;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getIconColor() {
        return iconColor;
    }

    public void setIconColor(Integer iconColor) {
        this.iconColor = iconColor;
    }
}
