package vn.com.vng.zalopay.menu.model;

import vn.com.vng.zalopay.domain.model.AbstractData;

/**
 * Created by longlv on 04/05/2016.
 * *
 */
public class MenuItem extends AbstractData {

    public int id;
    public MenuItemType itemType;
    public Integer iconResource;
    public Integer iconColor;
    public String title;
    public boolean showDivider;

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
    
}
