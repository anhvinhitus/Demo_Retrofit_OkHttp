package vn.com.vng.zalopay.paymentapps;

import android.util.LongSparseArray;

import com.zalopay.apploader.impl.BundleServiceImpl;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.utils.AndroidUtils;

/**
 * Created by AnhHieu on 7/1/16.
 * Static configuration for PaymentApps
 */
public class PaymentAppConfig {

    public static class Constants {
        public static final long TRANSFER_MONEY = 1;
        public static final long RECEIVE_MONEY = 3;
        public static final long RED_PACKET = 6;
        public static final long SHOW_SHOW = 22;

        public static final String FONT_FAMILY_NAME_ZALOPAY = "zalopay";
    }

    private static final LongSparseArray<AppResource> APP_RESOURCE_MAP;

    public static final List<AppResource> APP_RESOURCE_LIST;
    public static final List<AppResource> EXCLUDE_APP_RESOURCE_LIST;
    public static final List<String> EXCEPT_LOAD_FONTS;

    static {
        APP_RESOURCE_MAP = new LongSparseArray<>();
        APP_RESOURCE_MAP.put(Constants.TRANSFER_MONEY,
                new AppResource(Constants.TRANSFER_MONEY,
                        PaymentAppTypeEnum.INTERNAL_APP.value,
                        AndroidApplication.instance().getString(R.string.transfer_money),
                        AndroidApplication.instance().getString(R.string.app_1_transfers),
                        AndroidUtils.getColorFromResource(R.color.menu_font_ic_blue)));
        APP_RESOURCE_MAP.put(Constants.RECEIVE_MONEY,
                new AppResource(Constants.RECEIVE_MONEY,
                        PaymentAppTypeEnum.INTERNAL_APP.value,
                        AndroidApplication.instance().getString(R.string.receive_money),
                        AndroidApplication.instance().getString(R.string.app_1_receivemoney),
                        AndroidUtils.getColorFromResource(R.color.menu_font_ic_green)));
        APP_RESOURCE_MAP.put(Constants.RED_PACKET,
                new AppResource(Constants.RED_PACKET,
                        PaymentAppTypeEnum.REACT_NATIVE.value,
                        AndroidApplication.instance().getString(R.string.red_envelope),
                        AndroidApplication.instance().getString(R.string.app_6_red),
                        AndroidUtils.getColorFromResource(R.color.menu_font_ic_red)));

        APP_RESOURCE_MAP.put(Constants.SHOW_SHOW,
                new AppResource(Constants.SHOW_SHOW,
                        PaymentAppTypeEnum.REACT_NATIVE.value, "Show Show"));

        APP_RESOURCE_LIST = new ArrayList<>();
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.TRANSFER_MONEY));
        APP_RESOURCE_LIST.add(APP_RESOURCE_MAP.get(Constants.RECEIVE_MONEY));

        EXCLUDE_APP_RESOURCE_LIST = new ArrayList<>();
        EXCLUDE_APP_RESOURCE_LIST.add(new AppResource(BundleServiceImpl.ZALOPAY_INTERNAL_APPLICATION_ID,
                PaymentAppTypeEnum.REACT_NATIVE.value, "TK Zalo Pay"));

        EXCEPT_LOAD_FONTS = new ArrayList<>();
        EXCEPT_LOAD_FONTS.add(Constants.FONT_FAMILY_NAME_ZALOPAY);

    }

    public static AppResource getAppResource(long appId) {
        return APP_RESOURCE_MAP.get(appId);
    }

}
