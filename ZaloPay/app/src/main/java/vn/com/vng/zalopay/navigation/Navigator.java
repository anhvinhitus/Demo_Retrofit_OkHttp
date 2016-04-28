package vn.com.vng.zalopay.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.home.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.QRCodeScannerActivity;

/*
* Navigator
* */
@Singleton
public class Navigator {

    @Inject
    public Navigator() {
        //empty
    }


    public void startLoginActivity(Context context) {
        Intent intent = new Intent(context, LoginZaloActivity.class);
        context.startActivity(intent);
    }

    public void startHomeActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public void startQrCodeActivity(Context context) {
        Intent intent = new Intent(context, QRCodeScannerActivity.class);
        context.startActivity(intent);
    }

    public void startActivity(Fragment fragment, Intent intent) {
        fragment.startActivity(intent);
    }

    public void startActivityForResult(Fragment fragment, Intent intent, int requestCode) {
        fragment.startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(Activity act, Intent intent, int requestCode) {
        act.startActivityForResult(intent, requestCode);
    }
}
