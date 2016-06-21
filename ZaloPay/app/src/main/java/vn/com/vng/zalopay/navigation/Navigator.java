package vn.com.vng.zalopay.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.account.ui.activities.EditProfileActivity;
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.account.ui.activities.PinProfileActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.account.ui.activities.ProfileInfo2Activity;
import vn.com.vng.zalopay.account.ui.activities.ChangePinActivity;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.mdl.INavigator;
import vn.com.vng.zalopay.paymentapps.ui.PaymentApplicationActivity;
import vn.com.vng.zalopay.transfer.ui.activities.TransferActivity;
import vn.com.vng.zalopay.transfer.ui.activities.TransferHomeActivity;
import vn.com.vng.zalopay.transfer.ui.activities.ZaloContactActivity;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferHomeFragment;
import vn.com.vng.zalopay.transfer.ui.fragment.ZaloContactFragment;
import vn.com.vng.zalopay.ui.activity.LinkCardActivity;
import vn.com.vng.zalopay.ui.activity.LinkCardProcedureActivity;
import vn.com.vng.zalopay.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.activity.QRCodeScannerActivity;

/*
* Navigator
* */
@Singleton
public class Navigator implements INavigator {

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

    public void startUpdateProfileLevel2Activity(Context context, boolean clearTop) {
        Intent intent = new Intent(context, UpdateProfileLevel2Activity.class);

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
        Intent intent = getIntentMiniAppActivity(activity, moduleName);
        activity.startActivity(intent);
    }

    public Intent getIntentMiniAppActivity(Context context, String moduleName) {
        Intent intent = new Intent(context, MiniApplicationActivity.class);
        intent.putExtra("moduleName", moduleName);
        return intent;
    }

    public void startLinkCardActivity(Activity activity) {
        activity.startActivity(intentLinkCard(activity));
    }

    public void startLinkCardProcedureActivity(Fragment activity) {
        Intent intent = new Intent(activity.getContext(), LinkCardProcedureActivity.class);
        activity.startActivityForResult(intent, LinkCardActivity.REQUEST_CODE);
    }


    public void startPaymentApplicationActivity(Context context, String name) {
        Intent intent = new Intent(context, PaymentApplicationActivity.class);
        intent.putExtra("moduleName", name);
        context.startActivity(intent);
    }

    public void startPaymentApplicationActivity(Context context, AppResource appResource, String moduleName) {
        Intent intent = new Intent(context, PaymentApplicationActivity.class);
        intent.putExtra("moduleName", moduleName);
        intent.putExtra("appResource", appResource);
        context.startActivity(intent);
    }

    public void startUpdateProfileLevel2Activity(Context context, String walletTransID) {
        if (context == null) {
            Timber.w("Cannot start pre-profile activity due to NULL context");
            return;
        }

        Intent intent = new Intent(context, UpdateProfileLevel2Activity.class);
        if (!TextUtils.isEmpty(walletTransID)) {
            intent.putExtra(vn.com.vng.zalopay.domain.Constants.WALLETTRANSID, walletTransID);
        }

        context.startActivity(intent);
    }

    public void startPinProfileActivity(Activity activity) {
        Intent intent = new Intent(activity, PinProfileActivity.class);
        activity.startActivity(intent);
    }

    public void startProfileInfoActivity(Activity activity) {
        activity.startActivity(intentProfile(activity));
    }

    public void startEditProfileActivity(Activity activity) {
        Intent intent = new Intent(activity, EditProfileActivity.class);
        activity.startActivity(intent);
    }

    public void startChangePinActivity(Activity activity) {
        Intent intent = new Intent(activity, ChangePinActivity.class);
        activity.startActivity(intent);
    }

    public void startTransferMoneyActivity(Activity activity) {
        Intent intent = new Intent(activity, TransferHomeActivity.class);
        activity.startActivity(intent);
    }

    public void startZaloContactActivity(TransferHomeFragment fragment) {
        Intent intent = new Intent(fragment.getContext(), ZaloContactActivity.class);
        fragment.startActivityForResult(intent, Constants.REQUEST_CODE_TRANSFER);
    }

    public void startTransferActivity(ZaloContactFragment fragment, Bundle bundle) {
        Intent intent = new Intent(fragment.getContext(), TransferActivity.class);
        intent.putExtras(bundle);
        fragment.startActivityForResult(intent, Constants.REQUEST_CODE_TRANSFER);
    }

    public void startTransferActivity(TransferHomeFragment fragment, Bundle bundle) {
        Intent intent = new Intent(fragment.getContext(), TransferActivity.class);
        intent.putExtras(bundle);
        fragment.startActivity(intent);
    }

    @Override
    public Intent intentProfile(Context context) {
        Intent intent = new Intent(context, ProfileInfo2Activity.class);
        return intent;
    }

    @Override
    public Intent intentLinkCard(Context context) {
        Intent intent = new Intent(context, LinkCardActivity.class);
        return intent;
    }
}
