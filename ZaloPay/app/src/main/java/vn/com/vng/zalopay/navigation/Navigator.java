package vn.com.vng.zalopay.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.account.ui.activities.PreProfileActivity;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.home.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.LinkCardActivity;
import vn.com.vng.zalopay.ui.activity.LinkCardProcedureActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
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
        startLoginActivity(context, false);
    }

    public void startLoginActivity(Context context, boolean clearTop) {
        Intent intent = new Intent(context, LoginZaloActivity.class);
        if (clearTop) {
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        }

        context.startActivity(intent);
    }

    public void startHomeActivity(Context context) {
        startHomeActivity(context, false);
    }

    public void startHomeActivity(Context context, boolean clearTop) {
        Intent intent = new Intent(context, MainActivity.class);

        if (clearTop) {
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        }

        context.startActivity(intent);
    }


    public void startQrCodeActivity(Context context) {
        Intent intent = new Intent(context, QRCodeScannerActivity.class);
        context.startActivity(intent);
    }

    public void startDepositActivity(Context context) {
        Intent intent = new Intent(context, BalanceTopupActivity.class);
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

    public void startMiniAppActivity(Activity activity, String moduleName) {
        Intent intent = new Intent(activity, MiniApplicationActivity.class);
        intent.putExtra("moduleName", moduleName);
        activity.startActivity(intent);
    }

    public void startLinkCardActivity(Activity activity) {
        Intent intent = new Intent(activity, LinkCardActivity.class);
        activity.startActivity(intent);
    }

    public void startLinkCardProducedureActivity(Fragment activity) {
        Intent intent = new Intent(activity.getContext(), LinkCardProcedureActivity.class);
        activity.startActivityForResult(intent, LinkCardActivity.REQUEST_CODE);
    }

    public void startProfileActivity(Activity activity) {
        Intent intent = new Intent(activity, PreProfileActivity.class);
        activity.startActivity(intent);
    }
}
