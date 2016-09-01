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

        mMenuItems.add(new MenuItem(HOME_ID, MenuItemType.ITEM, R.drawable.ic_trangchu_menu, "Trang Chủ"));
        mMenuItems.add(new MenuItem(NOTIFICATION_ID, MenuItemType.ITEM, R.drawable.ic_thongbao_menu, R.drawable.ic_arrow_right, "Thông Báo", false));
        mMenuItems.add(new MenuItem(TRANSACTION_ID, MenuItemType.HEADER, null, "GIAO DỊCH"));
        mMenuItems.add(new MenuItem(SCAN_QR_ID, MenuItemType.ITEM, R.drawable.ic_thanhtoan_menu, "Trả Tiền"));
        mMenuItems.add(new MenuItem(DEPOSIT_ID, MenuItemType.ITEM, R.drawable.ic_naptien_menu, "Nạp Tiền"));

        mMenuItems.add(new MenuItem(TRANSFER_ID, MenuItemType.ITEM, R.drawable.ic_chuyentien_menu, "Chuyển Tiền"));

        mMenuItems.add(new MenuItem(SAVE_CARD_ID, MenuItemType.ITEM, R.drawable.ic_luuthe, "Lưu Thẻ"));
        mMenuItems.add(new MenuItem(TRANSACTION_HISTORY_ID, MenuItemType.ITEM, R.drawable.ic_lichsu_menu, R.drawable.ic_arrow_right, "Lịch Sử Thanh Toán", false));

        mMenuItems.add(new MenuItem(APPLICATION_ID, MenuItemType.HEADER, null, "ỨNG DỤNG"));
        mMenuItems.add(new MenuItem(FAQ_ID, MenuItemType.ITEM, R.drawable.ic_faq_menu, "FAQ"));
        mMenuItems.add(new MenuItem(TERM_OF_USE, MenuItemType.ITEM, R.drawable.ic_thoathuan_menu, "Thỏa Thuận Sử Dụng"));
        mMenuItems.add(new MenuItem(CONTACT_SUPPORT_ID, MenuItemType.ITEM, R.drawable.ic_lienhe_menu, "Liên Hệ Hỗ Trợ"));
        mMenuItems.add(new MenuItem(APPLICATION_INFO_ID, MenuItemType.ITEM, R.drawable.ic_thongtin_menu, "Thông Tin Ứng Dụng"));

//        mMenuItems.add(new MenuItem(ACCOUNT_ID, MenuItemType.HEADER, null, "TÀI KHOẢN"));
        mMenuItems.add(new MenuItem(SIGN_OUT_ID, MenuItemType.ITEM, R.drawable.ic_recycle, "Đăng Xuất", false));
    }
}
