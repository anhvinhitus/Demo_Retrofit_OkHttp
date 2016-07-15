package vn.com.vng.zalopay.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.ReactAppConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.activities.EditProfileActivity;
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.account.ui.activities.PinProfileActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.account.ui.activities.ProfileInfo2Activity;
import vn.com.vng.zalopay.account.ui.activities.ChangePinActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel3Activity;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.mdl.INavigator;
import vn.com.vng.zalopay.paymentapps.ui.PaymentApplicationActivity;
import vn.com.vng.zalopay.transfer.ui.activities.TransferActivity;
import vn.com.vng.zalopay.transfer.ui.activities.TransferHomeActivity;
import vn.com.vng.zalopay.transfer.ui.activities.ZaloContactActivity;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferHomeFragment;
import vn.com.vng.zalopay.transfer.ui.fragment.ZaloContactFragment;
import vn.com.vng.zalopay.ui.activity.InvitationCodeActivity;
import vn.com.vng.zalopay.ui.activity.LinkCardActivity;
import vn.com.vng.zalopay.ui.activity.LinkCardProcedureActivity;
import vn.com.vng.zalopay.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.activity.QRCodeScannerActivity;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/*
* Navigator
* */
@Singleton
public class Navigator implements INavigator {
    private final int MIN_PROFILE_LEVEL = 2;

    UserConfig userConfig;

    @Inject
    public Navigator(UserConfig userConfig) {
        //empty
        this.userConfig = userConfig;
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

    public void startLoginActivity(Context context, String message) {
        Intent intent = new Intent(context, LoginZaloActivity.class);
        intent.putExtra("finish", true);
        if (!TextUtils.isEmpty(message)) {
            intent.putExtra(Constants.ARG_MESSAGE, message);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_TASK_ON_HOME);

        context.startActivity(intent);
    }

    public void startHomeActivity(Context context, boolean clearTop) {
        Intent intent = intentHomeActivity(context, clearTop);
        context.startActivity(intent);
    }

    public void startHomeActivity(Context context, long appId, String zptranstoken) {
        Intent intent = intentHomeActivity(context, true);
        intent.putExtra(Constants.ARG_APPID, appId);
        intent.putExtra(Constants.ARG_ZPTRANSTOKEN, zptranstoken);
        context.startActivity(intent);
    }

    public Intent intentHomeActivity(Context context, boolean clearTop) {
        Intent intent = new Intent(context, MainActivity.class);

        if (clearTop) {
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        }
        return intent;
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showUpdateProfileInfoDialog(final Context context) {
        if (context == null) {
            return;
        }
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
                .setContentText(context.getString(R.string.txt_need_input_userinfo))
                .setCancelText(context.getString(R.string.txt_close))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .setConfirmText(context.getString(R.string.txt_input_userinfo))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                        startUpdateProfileLevel2Activity(context, false);
                    }
                });
        sweetAlertDialog.show();
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
        if (userConfig == null || userConfig.getCurrentUser() == null || userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
            showUpdateProfileInfoDialog(activity);
        } else {
            activity.startActivity(intentLinkCard(activity));
        }
    }

    public void startLinkCardProcedureActivity(Activity activity) {
        if (userConfig == null || userConfig.getCurrentUser() == null || userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
            showUpdateProfileInfoDialog(activity);
        } else {
            Intent intent = new Intent(activity, LinkCardProcedureActivity.class);
            activity.startActivityForResult(intent, LinkCardActivity.REQUEST_CODE);
        }
    }

    public void startLinkCardProcedureActivity(Fragment activity) {
        if (userConfig == null || userConfig.getCurrentUser() == null || userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
            if (activity != null) {
                showUpdateProfileInfoDialog(activity.getContext());
            }
        } else {
            Intent intent = new Intent(activity.getContext(), LinkCardProcedureActivity.class);
            activity.startActivityForResult(intent, LinkCardActivity.REQUEST_CODE);
        }
    }

    public void startPaymentApplicationActivity(Context context, int appId) {
        Map<String, String> options = new HashMap<>();
        options.put("view", "main");
        Intent intent = intentPaymentApp(context, appId, options);
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
        if (userConfig == null || userConfig.getCurrentUser() == null || userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
            if (activity != null) {
                showUpdateProfileInfoDialog(activity);
            }
        } else {
            Intent intent = new Intent(activity, TransferHomeActivity.class);
            activity.startActivity(intent);
        }
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

    public void startUpdateProfile3Activity(Context context) {
        Intent intent = new Intent(context, UpdateProfileLevel3Activity.class);
        context.startActivity(intent);
    }

    public void startInvitationCodeActivity(Context context) {
        Intent intent = new Intent(context, InvitationCodeActivity.class);
        context.startActivity(intent);
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

    @Override
    public Intent intentPaymentApp(Context context, int appId, Map<String, String> launchOptions) {
        AppResource appResource = ReactAppConfig.getAppResource(appId);
        if (appResource == null) {
            return null;
        }
        Intent intent = new Intent(context, PaymentApplicationActivity.class);
        intent.putExtra("appResource", appResource);
        Bundle options = new Bundle();
        for (Map.Entry<String, String> e : launchOptions.entrySet()) {
            options.putString(e.getKey(), e.getValue());
        }
        intent.putExtra("launchOptions", options);
        return intent;
    }
}
