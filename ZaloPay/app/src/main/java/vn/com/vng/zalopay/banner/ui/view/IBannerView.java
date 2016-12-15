package vn.com.vng.zalopay.banner.ui.view;

import android.app.Activity;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;

/**
 * Created by longlv on 12/14/16.
 * *
 */

public interface IBannerView {

    Activity getActivity();

    void showError(String msg);

    void showErrorDialog(String msg);

    void showBannerAds(List<DBanner> banners);

    void changeBanner();
}
