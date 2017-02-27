package vn.com.vng.zalopay.webapp;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 2/27/17.
 */

public class WebAppBottomSheetItemUtil {

    public static final int COPY_URL = 2;
    public static final int REFRESH = 3;
    public static final int OPEN_IN_BROWSER = 4;
    public static final int SHARE_ON_ZALO = 5;
    private static List<WebAppBottomSheetItem> mMenuItems;

    public static List<WebAppBottomSheetItem> getMenuItems() {
        return mMenuItems;
    }

    static {
        mMenuItems = new ArrayList<>();

        mMenuItems.add(new WebAppBottomSheetItem(SHARE_ON_ZALO, "Chia sẻ trên Zalo", R.drawable.ico_sharezalo));
        mMenuItems.add(new WebAppBottomSheetItem(COPY_URL, "Sao chép URL", R.string.webapp_copyurl, R.color.black));
        mMenuItems.add(new WebAppBottomSheetItem(REFRESH, "Làm mới", R.string.webapp_refresh, R.color.black));
        mMenuItems.add(new WebAppBottomSheetItem(OPEN_IN_BROWSER, "Mở trong trình duyệt", R.string.webapp_browser, R.color.black));
   }
}
