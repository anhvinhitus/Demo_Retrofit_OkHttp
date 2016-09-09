package vn.com.vng.zalopay.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.zalopay.apploader.internal.ModuleName;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.account.ui.activities.ChangePinActivity;
import vn.com.vng.zalopay.account.ui.activities.EditAccountNameActivity;
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.account.ui.activities.ProfileActivity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel2Activity;
import vn.com.vng.zalopay.account.ui.activities.UpdateProfileLevel3Activity;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.data.NetworkError;
import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.data.eventbus.TokenExpiredEvent;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.paymentapps.ui.PaymentApplicationActivity;
import vn.com.vng.zalopay.scanners.ui.ScanToPayActivity;
import vn.com.vng.zalopay.transfer.ui.ReceiveMoneyActivity;
import vn.com.vng.zalopay.transfer.ui.TransferActivity;
import vn.com.vng.zalopay.transfer.ui.TransferHomeActivity;
import vn.com.vng.zalopay.transfer.ui.ZaloContactActivity;
import vn.com.vng.zalopay.transfer.ui.TransferHomeFragment;
import vn.com.vng.zalopay.ui.activity.IntroActivity;
import vn.com.vng.zalopay.ui.activity.InvitationCodeActivity;
import vn.com.vng.zalopay.ui.activity.LinkCardActivity;
import vn.com.vng.zalopay.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.activity.QRCodeScannerActivity;
import vn.com.vng.zalopay.withdraw.ui.activities.WithdrawActivity;
import vn.com.vng.zalopay.withdraw.ui.activities.WithdrawConditionActivity;
import vn.com.vng.zalopay.ui.activity.BalanceManagementActivity;
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

    public void startScanToPayActivity(Activity activity) {
        Intent intent = new Intent(activity, ScanToPayActivity.class);
        activity.startActivity(intent);
    }

    private void showUpdateProfileInfoDialog(final Context context) {
        if (context == null) {
            return;
        }
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog)
                .setTitleText(context.getString(R.string.notification))
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

    private void showUpdateProfileLevel3Dialog(final Context context) {
        if (context == null) {
            return;
        }
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog)
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
                        startUpdateProfile3Activity(context);
                    }
                });
        sweetAlertDialog.show();
    }

    public void startMiniAppActivity(Activity activity, String moduleName) {
        if (ModuleName.RED_PACKET.equals(moduleName)) {
            if (userConfig == null ||
                    userConfig.getCurrentUser() == null ||
                    userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
                showUpdateProfileInfoDialog(activity);
                return;
            }
        }
        Intent intent = getIntentMiniAppActivity(activity, moduleName, new HashMap<String, String>());
        activity.startActivity(intent);
    }

    public void startLinkCardActivity(Activity activity) {
        if (userConfig == null || userConfig.getCurrentUser() == null || userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
            showUpdateProfileInfoDialog(activity);
        } else {
            activity.startActivity(intentLinkCard(activity));
        }
    }

    public void startPaymentApplicationActivity(Context context, AppResource appResource) {
        Map<String, String> options = new HashMap<>();
        options.put("view", "main");
        Intent intent = intentPaymentApp(context, appResource, options);
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

    @Override
    public void startProfileInfoActivity(Context context) {
        context.startActivity(intentProfile(context));
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

    public void startTransferActivity(Fragment fragment, Bundle bundle) {
        Intent intent = new Intent(fragment.getContext(), TransferActivity.class);
        intent.putExtras(bundle);
        fragment.startActivityForResult(intent, Constants.REQUEST_CODE_TRANSFER);
    }

    public void startTransferActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, TransferActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public void startTransferActivity(Context context, Person person) {
        Intent intent = new Intent(context, TransferActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("person", Parcels.wrap(person));
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public void startUpdateProfile3Activity(Context context) {
        if (userConfig.hasCurrentUser() && userConfig.getCurrentUser().profilelevel == 2) {
            Intent intent = new Intent(context, UpdateProfileLevel3Activity.class);
            context.startActivity(intent);
        }
    }

    public void startInvitationCodeActivity(Context context) {
        Intent intent = new Intent(context, InvitationCodeActivity.class);
        context.startActivity(intent);
    }

    @Override
    public Intent intentProfile(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        return intent;
    }

    @Override
    public Intent intentLinkCard(Context context) {
        Intent intent = new Intent(context, LinkCardActivity.class);
        return intent;
    }

    @Override
    public Intent intentPaymentApp(Context context, AppResource appResource, Map<String, String> launchOptions) {
        if (appResource == null) {
            return null;
        }
        Intent intent = new Intent(context, PaymentApplicationActivity.class);
        intent.putExtra("appResource", Parcels.wrap(appResource));
        Bundle options = new Bundle();
        for (Map.Entry<String, String> e : launchOptions.entrySet()) {
            options.putString(e.getKey(), e.getValue());
        }
        intent.putExtra("launchOptions", options);
        return intent;
    }

    public void startIntroActivity(Context context) {
        Intent intent = new Intent(context, IntroActivity.class);
        context.startActivity(intent);
    }

    public void startIntroActivityForResult(Fragment fragment) {
        Intent intent = new Intent(fragment.getContext(), IntroActivity.class);
        fragment.startActivityForResult(intent, Constants.REQUEST_CODE_INTRO);
    }

    public void startBalanceManagementActivity(Context context) {
        if (userConfig == null || userConfig.getCurrentUser() == null) {
            EventBus.getDefault().post(new TokenExpiredEvent(NetworkError.TOKEN_INVALID));
            return;
        }
        Intent intent = new Intent(context, BalanceManagementActivity.class);
        context.startActivity(intent);
    }

    public void startWithdrawConditionActivity(Context context) {
        Intent intent = new Intent(context, WithdrawConditionActivity.class);
        context.startActivity(intent);
    }

    public void startWithdrawActivity(Context context) {
        Intent intent = new Intent(context, WithdrawActivity.class);
        context.startActivity(intent);
    }

    public void startEditAccountActivity(Context context) {
        Intent intent = new Intent(context, EditAccountNameActivity.class);
        context.startActivity(intent);
    }

    public void startTransactionDetail(Context context, String transid) {
        Map<String, String> launchOptions = new HashMap<>();
        launchOptions.put("view", "history");
        launchOptions.put("transid", transid);
        Intent intent = getIntentMiniAppActivity(context, ModuleName.TRANSACTION_LOGS, launchOptions);
        context.startActivity(intent);
    }

    public void startTermActivity(Context context) {
        Map<String, String> option = new HashMap<>();
        option.put("view", "termsOfUse");
        Intent intent = getIntentMiniAppActivity(context, ModuleName.ABOUT, option);
        context.startActivity(intent);
    }

    public Intent getIntentMiniAppActivity(Context context, String moduleName, Map<String, String> launchOptions) {
        Intent intent = new Intent(context, MiniApplicationActivity.class);
        intent.putExtra("moduleName", moduleName);
        Bundle options = new Bundle();
        for (Map.Entry<String, String> e : launchOptions.entrySet()) {
            options.putString(e.getKey(), e.getValue());
        }
        intent.putExtra("launchOptions", options);
        return intent;
    }

    public void startDialSupport(Context context) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + context.getString(R.string.phone_support)));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(callIntent);
        } catch (Exception e) {
        }
    }

    public void startMyQrCode(Context context) {
        Intent intent = new Intent(context, ReceiveMoneyActivity.class);
        context.startActivity(intent);
    }
}
