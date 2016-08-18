package vn.com.vng.zalopay.menu.model;

/**
 * Created by longlv on 04/05/2016.
 */
public enum MenuItemType {
    HEADER(0), ITEM(1), OTHERS(-1);
    private final int value;

    MenuItemType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}