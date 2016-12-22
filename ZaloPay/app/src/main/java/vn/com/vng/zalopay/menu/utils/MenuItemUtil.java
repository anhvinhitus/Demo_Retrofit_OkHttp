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

    public static final int HOME_ID = 0;
    public static final int NOTIFICATION_ID = 1;
    public static final int TRANSACTION_ID = 2;
    public static final int SCAN_QR_ID = 3;
    public static final int DEPOSIT_ID = 4;
    public static final int SAVE_CARD_ID = 14;
    public static final int TRANSFER_ID = 5;
    public static final int TRANSACTION_HISTORY_ID = 6;
    public static final int APPLICATION_ID = 7;
    public static final int FAQ_ID = 8;
    public static final int CONTACT_SUPPORT_ID = 9;
    public static final int APPLICATION_INFO_ID = 10;
    public static final int ACCOUNT_ID = 11;
    public static final int SIGN_OUT_ID = 12;
    public static final int TERM_OF_USE = 15;

    private static List<MenuItem> mMenuItems;

    public static List<MenuItem> getMenuItems() {
        return mMenuItems;
    }

    static {
        mMenuItems = new ArrayList<>();

        mMenuItems.add(new MenuItem(HOME_ID, MenuItemType.ITEM, "Trang Chủ", R.string.icon_home, R.color.menu_font_ic_blue));
        mMenuItems.add(new MenuItem(NOTIFICATION_ID, MenuItemType.ITEM, "Thông Báo", R.string.icon_notification, R.color.menu_font_ic_red, false));
        mMenuItems.add(new MenuItem(TRANSACTION_ID, MenuItemType.HEADER, "GIAO DỊCH"));
        mMenuItems.add(new MenuItem(SCAN_QR_ID, MenuItemType.ITEM, "Trả Tiền", R.string.icon_pay, R.color.menu_font_ic_green));
        mMenuItems.add(new MenuItem(DEPOSIT_ID, MenuItemType.ITEM, "Nạp Tiền", R.string.icon_deposit, R.color.menu_font_ic_green));

        mMenuItems.add(new MenuItem(TRANSFER_ID, MenuItemType.ITEM, "Chuyển Tiền", R.string.icon_transfer, R.color.menu_font_ic_blue));

        mMenuItems.add(new MenuItem(SAVE_CARD_ID, MenuItemType.ITEM, "Liên Kết Thẻ", R.string.icon_link_card, R.color.menu_font_ic_yellow));
        mMenuItems.add(new MenuItem(TRANSACTION_HISTORY_ID, MenuItemType.ITEM, "Lịch Sử Thanh Toán", R.string.icon_trans_history, R.color.menu_font_ic_red, false));

        mMenuItems.add(new MenuItem(APPLICATION_ID, MenuItemType.HEADER, "ỨNG DỤNG"));
        mMenuItems.add(new MenuItem(FAQ_ID, MenuItemType.ITEM, "FAQ", R.string.icon_faq, R.color.menu_font_ic_gray));
        mMenuItems.add(new MenuItem(TERM_OF_USE, MenuItemType.ITEM, "Thỏa Thuận Sử Dụng", R.string.icon_term_of_user, R.color.menu_font_ic_gray));
        mMenuItems.add(new MenuItem(CONTACT_SUPPORT_ID, MenuItemType.ITEM, "Liên Hệ Hỗ Trợ", R.string.icon_contact_support, R.color.menu_font_ic_gray));
        mMenuItems.add(new MenuItem(APPLICATION_INFO_ID, MenuItemType.ITEM, "Thông Tin Ứng Dụng", R.string.icon_app_info, R.color.menu_font_ic_gray));

//        mMenuItems.add(new MenuItem(ACCOUNT_ID, MenuItemType.HEADER, null, "TÀI KHOẢN"));
        mMenuItems.add(new MenuItem(SIGN_OUT_ID, MenuItemType.ITEM, "Đăng Xuất", R.string.icon_sign_out, R.color.menu_font_ic_gray, false));
    }
}
