package vn.com.vng.zalopay.menu.utils;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.menu.model.MenuItem;
import vn.com.vng.zalopay.menu.model.MenuItemType;

/**
 * Created by longlv on 04/05/2016.
 */
public class MenuItemUtil {

    public static final int HOME_ID = 0;
    public static final int NOTIFICATION_ID = 1;
    public static final int TRANSACTION_ID = 2;
    public static final int SCAN_QR_ID = 3;
    public static final int DEPOSIT_ID = 4;
    public static final int TRANSFER_ID = 5;
    public static final int TRANSACTION_HISTORY_ID = 6;
    public static final int APPLICATION_ID = 7;
    public static final int FAQ_ID = 8;
    public static final int CONTACT_SUPPORT_ID = 9;
    public static final int APPLICATION_INFO_ID = 10;
    public static final int ACCOUNT_ID = 11;
    public static final int SIGOUT_ID = 12;

    private static List<MenuItem> mMenuItems;

    public static List<MenuItem> getMenuItems() {
        return mMenuItems;
    }

    static {
        mMenuItems = new ArrayList<>();
        mMenuItems.add(new MenuItem(HOME_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Trang Chủ"));
        mMenuItems.add(new MenuItem(NOTIFICATION_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Thông Báo"));

        mMenuItems.add(new MenuItem(TRANSACTION_ID, MenuItemType.HEADER, R.drawable.ic_launcher, "GIAO DỊCH"));
        mMenuItems.add(new MenuItem(SCAN_QR_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Quét Mã QR"));
        mMenuItems.add(new MenuItem(DEPOSIT_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Nạp Tiền"));
        mMenuItems.add(new MenuItem(TRANSFER_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Chuyển Tiền"));
        mMenuItems.add(new MenuItem(TRANSACTION_HISTORY_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Lịch Sử Giao Dịch"));

        mMenuItems.add(new MenuItem(APPLICATION_ID, MenuItemType.HEADER, R.drawable.ic_launcher, "ỨNG DỤNG"));
        mMenuItems.add(new MenuItem(FAQ_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "FAQ"));
        mMenuItems.add(new MenuItem(CONTACT_SUPPORT_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Liên Hệ Hỗ Trợ"));
        mMenuItems.add(new MenuItem(APPLICATION_INFO_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Thông Tin ứng Dụng"));

        mMenuItems.add(new MenuItem(ACCOUNT_ID, MenuItemType.HEADER, R.drawable.ic_launcher, "TÀI KHOẢN"));
        mMenuItems.add(new MenuItem(SIGOUT_ID, MenuItemType.ITEM, R.drawable.ic_launcher, "Đăng Xuất"));
    }
}
