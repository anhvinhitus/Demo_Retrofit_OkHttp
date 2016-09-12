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
import java.util.List;
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
import vn.com.vng.zalopay.paymentapps.ui.PaymentApplicationActivity;
import vn.com.vng.zalopay.scanners.ui.ScanToPayActivity;
import vn.com.vng.zalopay.transfer.ui.ReceiveMoneyActivity;
import vn.com.vng.zalopay.transfer.ui.TransferActivity;
import vn.com.vng.zalopay.transfer.ui.TransferHomeActivity;
import vn.com.vng.zalopay.transfer.ui.TransferHomeFragment;
import vn.com.vng.zalopay.transfer.ui.ZaloContactActivity;
import vn.com.vng.zalopay.ui.activity.BalanceManagementActivity;
import vn.com.vng.zalopay.ui.activity.IntroAppActivity;
import vn.com.vng.zalopay.ui.activity.IntroSaveCardActivity;
import vn.com.vng.zalopay.ui.activity.InvitationCodeActivity;
import vn.com.vng.zalopay.ui.activity.LinkCardActivity;
import vn.com.vng.zalopay.ui.activity.MainActivity;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;
import vn.com.vng.zalopay.ui.activity.QRCodeScannerActivity;
import vn.com.vng.zalopay.ui.dialog.PinProfileDialog;
import vn.com.vng.zalopay.withdraw.ui.activities.WithdrawActivity;
import vn.com.vng.zalopay.withdraw.ui.activities.WithdrawConditionActivity;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/*
* Navigator
* */
@Singleton
public class Navigator implements INavigator {
    private final int MIN_PROFILE_LEVEL = 2;

    UserConfig userConfig;

    private long lastTimeCheckPassword = 0;

    final long INTERVAL_CHECK_PASSWORD = 5 * 60 * 1000;

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

    public void startMiniAppActivity(Activity activity, String moduleName) {
        if (ModuleName.RED_PACKET.equals(moduleName)) {
            if (userConfig.hasCurrentUser()) {
                if (userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
                    showUpdateProfileInfoDialog(activity);
                    return;
                }
            }
        }
        Intent intent = getIntentMiniAppActivity(activity, moduleName, new HashMap<String, String>());
        activity.startActivity(intent);
    }

    public void startLinkCardActivity(Context context) {
        if (userConfig.hasCurrentUser()) {
            if (userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
                showUpdateProfileInfoDialog(context);
            } else {

                long now = System.currentTimeMillis();
                int numberCard = 0;
                try {
                    CShareData shareData = CShareData.getInstance((Activity) context);
                    List<DMappedCard> mapCardLis = shareData.getMappedCardList(userConfig.getCurrentUser().zaloPayId);
                    numberCard = mapCardLis.size();
                } catch (Exception ex) {
                    Timber.d(ex, "startLinkCardActivity");
                }

                if (numberCard <= 0 || now - lastTimeCheckPassword < INTERVAL_CHECK_PASSWORD) {
                    context.startActivity(intentLinkCard(context));
                } else {
                    new PinProfileDialog(context, intentLinkCard(context)).show();
                }
            }
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
        if (userConfig.hasCurrentUser()) {
            if (checkAndOpenPinDialog(context, intentProfile(context), userConfig.getCurrentUser().profilelevel)) {
                context.startActivity(intentProfile(context));
            }
        }
    }

    public void startChangePinActivity(Activity activity) {
        Intent intent = new Intent(activity, ChangePinActivity.class);
        activity.startActivity(intent);
    }

    public void startTransferMoneyActivity(Activity activity) {
        if (userConfig.hasCurrentUser()) {
            if (userConfig.getCurrentUser().profilelevel < MIN_PROFILE_LEVEL) {
                showUpdateProfileInfoDialog(activity);
            } else {
                Intent intent = new Intent(activity, TransferHomeActivity.class);
                activity.startActivity(intent);
            }
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

    public void startUpdateProfile3Activity(Context context) {
        if (userConfig.hasCurrentUser() && userConfig.getCurrentUser().profilelevel == MIN_PROFILE_LEVEL) {
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
        return new Intent(context, ProfileActivity.class);
    }

    @Override
    public Intent intentLinkCard(Context context) {
        return new Intent(context, LinkCardActivity.class);
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
        Intent intent = new Intent(context, IntroSaveCardActivity.class);
        context.startActivity(intent);
    }

    public void startIntroActivityForResult(Fragment fragment) {
        Intent intent = new Intent(fragment.getContext(), IntroSaveCardActivity.class);
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

    public void startTransactionHistoryList(Context context) {
        if (userConfig.hasCurrentUser()) {
            Intent intent = getIntentMiniAppActivity(context, ModuleName.TRANSACTION_LOGS, new HashMap<String, String>());
            if (checkAndOpenPinDialog(context, intent, userConfig.getCurrentUser().profilelevel)) {
                context.startActivity(intent);
            }
        }
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
            Timber.d(e, "startDialSupport");
        }
    }

    public void startMyQrCode(Context context) {
        Intent intent = new Intent(context, ReceiveMoneyActivity.class);
        context.startActivity(intent);
    }

    public void startIntroAppActivity(Context context) {
        Intent intent = new Intent(context, IntroAppActivity.class);
        context.startActivity(intent);
    }

    boolean checkAndOpenPinDialog(Context context, Intent pendingIntent, int level) {
        long now = System.currentTimeMillis();
        if (now - lastTimeCheckPassword >= INTERVAL_CHECK_PASSWORD && level >= MIN_PROFILE_LEVEL) {
            new PinProfileDialog(context, pendingIntent).show();
            return false;
        }
        return true;
    }

    public void setLastTimeCheckPin(long time) {
        lastTimeCheckPassword = time;
    }
}
