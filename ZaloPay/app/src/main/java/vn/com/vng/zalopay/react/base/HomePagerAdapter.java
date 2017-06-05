package vn.com.vng.zalopay.react.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.viewpager.AbsFragmentPagerAdapter;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.account.ui.fragment.ProfileFragment;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.promotion.PromotionFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.PersonalFragment;
import vn.com.vng.zalopay.ui.fragment.tabmain.ZaloPayFragment;
import vn.com.vng.zalopay.webview.ui.WebViewFragment;

/**
 * Created by hieuvm on 2/28/17.
 * *
 */

public class HomePagerAdapter extends AbsFragmentPagerAdapter {

    private static final int TAB_COUNT = 4;
    public static final int TAB_MAIN_INDEX = 0;
    public static final int TAB_TRANSACTION_INDEX = 1;
    public static final int TAB_PROMOTION_INDEX = 2;
    public static final int TAB_PERSONAL_INDEX = 3;

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundlePromotion = new Bundle();
        bundlePromotion.putString(Constants.ARG_URL, "https://zalopay.vn");

        switch (position) {
            case TAB_MAIN_INDEX:
                return ZaloPayFragment.newInstance();
            case TAB_TRANSACTION_INDEX:
                return InternalReactFragment.newInstance(ModuleName.TRANSACTION_LOGS);
            case TAB_PROMOTION_INDEX:
//                return PromotionFragment.newInstance();
                return WebViewFragment.newInstance(bundlePromotion);
            case TAB_PERSONAL_INDEX:
                return PersonalFragment.newInstance();
        }

        return null;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }

}
