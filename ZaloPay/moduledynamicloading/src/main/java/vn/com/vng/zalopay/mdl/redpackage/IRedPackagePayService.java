package vn.com.vng.zalopay.mdl.redpackage;

import android.app.Activity;

import vn.com.vng.zalopay.domain.model.BundleOrder;

/**
 * Created by longlv on 19/07/2016.
 * define methods that RedPackage will use to pay
 */
public interface IRedPackagePayService {
    void pay(Activity activity, BundleOrder bundleOrder, IRedPackagePayListener listener);
}
