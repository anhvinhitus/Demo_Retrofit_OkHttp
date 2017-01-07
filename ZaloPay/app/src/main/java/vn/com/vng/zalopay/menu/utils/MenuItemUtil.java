package vn.com.vng.zalopay.menu.utils;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.model.MenuItemType;

/**
 * Created by longlv on 04/05/2016.
 *
 */
public class MenuItemUtil {

    public static final int TRANSACTION_ID = 2;
    public static final int SCAN_QR_ID = 3;
    public static final int DEPOSIT_ID = 4;
    public static final int SAVE_CARD_ID = 14;
    public static final int TRANSFER_ID = 5;
    public static final int TRANSACTION_HISTORY_ID = 6;
    public static final int APPLICATION_ID = 7;
    public static final int SUPPORT_CENTER = 8;
    public static final int FEED_BACK = 9;
    public static final int APPLICATION_INFO_ID = 10;
    public static final int SIGN_OUT_ID = 12;

    private static List<MenuItem> mMenuItems;

    public static List<MenuItem> getMenuItems() {
        return mMenuItems;
    }

    static {
        mMenuItems = new ArrayList<>();

        mMenuItems.add(new MenuItem(SAVE_CARD_ID, MenuItemType.ITEM, "Liên Kết Thẻ", R.string.menu_linkcard, R.color.menu_font_ic_yellow));
        mMenuItems.add(new MenuItem(TRANSACTION_HISTORY_ID, MenuItemType.ITEM, "Lịch Sử Thanh Toán", R.string.menu_history, R.color.menu_font_ic_red, false));

        mMenuItems.add(new MenuItem(TRANSACTION_ID, MenuItemType.HEADER, "GIAO DỊCH"));
        mMenuItems.add(new MenuItem(DEPOSIT_ID, MenuItemType.ITEM, "Nạp Tiền", R.string.menu_1_receivemoney, R.color.menu_font_ic_green));
        mMenuItems.add(new MenuItem(SCAN_QR_ID, MenuItemType.ITEM, "Trả Tiền", R.string.menu_payqrcode, R.color.menu_font_ic_yellow));
        mMenuItems.add(new MenuItem(TRANSFER_ID, MenuItemType.ITEM, "Chuyển Tiền", R.string.menu_1_transfers, R.color.menu_font_ic_blue));

        mMenuItems.add(new MenuItem(APPLICATION_ID, MenuItemType.HEADER, "HỖ TRỢ"));
        mMenuItems.add(new MenuItem(SUPPORT_CENTER, MenuItemType.ITEM, "Trung Tâm Hỗ Trợ", R.string.menu_supportcenter, R.color.menu_font_ic_gray));
        //mMenuItems.add(new MenuItem(FEED_BACK, MenuItemType.ITEM, "Góp Ý", R.string.menu_feedback, R.color.menu_font_ic_gray));
        mMenuItems.add(new MenuItem(APPLICATION_INFO_ID, MenuItemType.ITEM, "Thông Tin Ứng Dụng", R.string.menu_infomation, R.color.menu_font_ic_gray));
        mMenuItems.add(new MenuItem(SIGN_OUT_ID, MenuItemType.ITEM, "Đăng Xuất", R.string.menu_logout, R.color.menu_font_ic_gray, false));
    }
}
