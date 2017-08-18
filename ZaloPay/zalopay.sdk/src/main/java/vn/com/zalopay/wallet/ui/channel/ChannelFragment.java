package vn.com.zalopay.wallet.ui.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.OnProgressDialogTimeoutListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import timber.log.Timber;
import vn.com.zalopay.analytics.ZPScreens;
import vn.com.zalopay.utility.CurrencyUtil;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.GlobalData;
import vn.com.zalopay.wallet.RS;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.helper.FontHelper;
import vn.com.zalopay.wallet.listener.OnSnackbarListener;
import vn.com.zalopay.wallet.listener.OnNetworkDialogListener;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.BaseFragment;
import vn.com.zalopay.wallet.ui.channellist.AbstractPaymentFragment;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;

import static vn.com.zalopay.wallet.helper.FontHelper.applyFont;

/*
 * Created by chucvv on 6/12/17.
 */

public class ChannelFragment extends AbstractPaymentFragment<ChannelPresenter> implements ChannelContract.IView {
    @LayoutRes
    int mLayoutId = R.layout.screen__card;
    private String mOriginTitle;
    private Bundle mData;
    private boolean mShowMenuItem = false;

    public static BaseFragment newInstance() {
        return new ChannelFragment();
    }

    public static BaseFragment newInstance(Bundle args) {
        BaseFragment fragment = newInstance();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @NonNull
    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.BANK_INPUTCARDINFO;
    }

    @Override
    public void onStartFeedbackSupport() {
        try {
            if (!existPresenter()) {
                Timber.w("invalid presenter");
                return;
            }
            mPresenter.showFeedbackDialog();
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    @Override
    public void onPaymentButtonClick() {
        mPresenter.onSubmitClick();
    }

    public ChannelPresenter sharePresenter() {
        return mPresenter;
    }

    @Override
    protected void onArguments() {
        super.onArguments();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mLayoutId = bundle.getInt(Constants.CHANNEL_CONST.layout);
            mData = bundle;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (!existPresenter()) {
            return false;
        }
        return mPresenter.onBackPressed();
    }

    @Override
    public int getLayoutId() {
        return mLayoutId;
    }

    @Override
    protected void onViewBound(View view) {
        super.onViewBound(view);
        mRootView = view;
        findViewById(R.id.zpsdk_btn_submit).setOnClickListener(mPaymentButtonClick);
        findViewById(R.id.zpw_payment_fail_rl_support).setOnClickListener(mSupportViewClick);
        //findViewById(R.id.zpw_payment_fail_rl_update_info).setOnClickListener(updateInfoClick);
        mPresenter.pushArgument(mData);
        setHasOptionsMenu(true);
        makeFont();
    }

    /***
     * apply font bold for some view
     */
    private void makeFont() {
        applyFont(findViewById(R.id.order_amount_total_txt), GlobalData.getStringResource(RS.string.sdk_font_medium));
    }

    @Override
    public void setTitle(String title) {
        if (TextUtils.isEmpty(mOriginTitle) && !TextUtils.isEmpty(title)) {
            mOriginTitle = title;
        }
        if (getActivity() != null && !TextUtils.isEmpty(title)) {
            ((ChannelActivity) getActivity()).setToolbarTitle(title);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mShowMenuItem) {
            inflater.inflate(R.menu.bidv_menu, menu);
            MenuItem menuItem = menu.findItem(R.id.menu_action_instruct);
            if(menuItem == null){
                return;
            }
            View view = menuItem.getActionView();
            if(view == null){
                return;
            }
            view.setOnClickListener(v -> {
                if (!existPresenter()) {
                    Timber.w("invalid presenter");
                    return;
                }
                mPresenter.showInstructRegisterBIDV();
            });
        }
    }

    @Override
    protected ChannelPresenter initializePresenter() {
        return new ChannelPresenter();
    }

    @Override
    protected void onDataBound(View view) {
        if (!existPresenter()) {
            Timber.w("invalid presenter");
            return;
        }
        mPresenter.startPayment();
    }

    @Override
    public void showLoading(String pTitle) {
        showLoading(pTitle, () -> {
            try {
                showError(getResources().getString(R.string.sdk_loading_timeout));
            } catch (Exception e) {
                Timber.d(e, "Exception show timeout loading dialog");
            }
        });
    }

    @Override
    public void showLoading(String pTitle, OnProgressDialogTimeoutListener timeoutListener) {
        if (getActivity() != null) {
            setTitle(pTitle);
            DialogManager.showProcessDialog(getActivity(), timeoutListener);
        }
    }

    @Override
    public void hideLoading() {
        DialogManager.closeLoadDialog();
        setTitle(mOriginTitle);
    }

    @Override
    public void showError(String pMessage) {
        hideLoading();
        DialogManager.showSweetDialogCustom(getActivity(), pMessage, getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.WARNING_TYPE, this::callbackThenTerminate);
    }

    @Override
    public void terminate() {
        Timber.d("recycle activity");
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void callbackThenTerminate() {
        try {
            if (existPresenter()) {
                mPresenter.setCallBack(Activity.RESULT_OK);
            }
        } catch (Exception e) {
            Timber.d(e);
        }
        terminate();
    }

    @Override
    public void showInfoDialog(String pMessage) {
        hideLoading();
        showInfoDialog(pMessage, null);
    }

    @Override
    public void showInfoDialog(String pMessage, ZPWOnEventDialogListener zpwOnEventDialogListener) {
        hideLoading();
        DialogManager.showSweetDialogCustom(getActivity(),
                pMessage, getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.INFO_TYPE, zpwOnEventDialogListener);
    }

    @Override
    public void showInfoDialog(String pMessage, String pLeftButton, ZPWOnEventDialogListener zpwOnEventDialogListener) {
        hideLoading();
        DialogManager.showSweetDialogCustom(getActivity(),
                pMessage, pLeftButton,
                SweetAlertDialog.INFO_TYPE, zpwOnEventDialogListener);
    }

    @Override
    public void showQuitConfirm(String message, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showConfirmDialog(getActivity(),
                message,
                getString(R.string.dialog_khong_button),
                getString(R.string.dialog_co_button), pListener);
    }

    @Override
    public void showNotificationDialog(String pMessage, String pLeftButton, ZPWOnEventDialogListener zpwOnEventDialogListener) {
        DialogManager.showSweetDialogCustom(getActivity(),
                pMessage,
                pLeftButton,
                SweetAlertDialog.NORMAL_TYPE,
                zpwOnEventDialogListener);
    }

    @Override
    public void showDialogManyOption(ZPWOnSweetDialogListener pListener) {
        DialogManager.showMultiButtonDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, -1,
                getResources().getString(R.string.sdk_trans_confirm_quit_load_website3ds_mess), pListener,
                getResources().getString(R.string.dialog_khong_button),
                getResources().getString(R.string.dialog_co_button),
                getResources().getString(R.string.dialog_getstatus_button));
    }

    @Override
    public String getFailMess() {
        TextView textView = (TextView) findViewById(R.id.sdk_trans_fail_reason_message_textview);
        return ((textView != null) ? textView.getText().toString() : "");
    }

    @Override
    public void renderTotalAmountAndFee(double total_amount, double fee) {
        if (fee > 0) {
            String txtFee = CurrencyUtil.formatCurrency(fee);
            setText(R.id.order_fee_txt, txtFee);
        } else {
            setText(R.id.order_fee_txt, getResources().getString(R.string.sdk_order_fee_free));
        }
        //order amount
        boolean hasAmount = total_amount > 0;
        if (hasAmount) {
            String order_amount = CurrencyUtil.formatCurrency(total_amount, false);
            setText(R.id.order_amount_total_txt, order_amount);
            ((TextView) findViewById(R.id.order_amount_total_txt)).setTextSize(getResources().getDimension(FontHelper.getFontSizeAmount(total_amount)));
        }
        setVisible(R.id.order_amount_total_linearlayout, hasAmount);
    }

    @Override
    public void renderOrderInfo(AbstractOrder order) {
        if (order == null) {
            Timber.d("order is null - skip render order info");
            return;
        }
        //order desc
        boolean hasDesc = !TextUtils.isEmpty(order.description);
        if (hasDesc) {
            setText(R.id.order_description_txt, order.description);
        }
        setVisible(R.id.order_description_txt, hasDesc);
        //order amount
        order.amount_total = order.amount + order.fee;
        renderTotalAmountAndFee(order.amount_total, order.fee);
    }

    @Override
    public void changeBgPaymentButton(boolean finalStep) {
        View view = findViewById(R.id.zpsdk_btn_submit);
        if (view == null) {
            return;
        }
        if (finalStep) {
            view.setBackgroundResource(R.drawable.bg_btn_green_border_selector);
        } else {
            view.setBackgroundResource(R.drawable.bg_btn_blue_border_selector);
        }
    }

    @Override
    public void disablePaymentButton() {
        View view = findViewById(R.id.zpsdk_btn_submit);
        if (view == null) {
            return;
        }
        view.setEnabled(false);
        view.setBackgroundResource(R.drawable.zpw_bg_button_disable);
    }

    @Override
    public void visibleOrderInfo(boolean visible) {
        setVisible(R.id.orderinfo_module, visible);
    }

    @Override
    public void updateCardNumberFont() {
        new Handler().postDelayed(() -> applyFont(findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.sdk_font_medium)), 500);
    }

    @Override
    public void showMaintenanceServiceDialog(String message) {
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.sdk_system_maintenance_mess);
        }
        showInfoDialog(message, () -> {
            if (existPresenter()) {
                mPresenter.setPaymentStatusAndCallback(PaymentStatus.SERVICE_MAINTENANCE);
            }
        });
    }

    @Override
    public void showRetryDialog(String pMessage, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showRetryDialog(getActivity(), pMessage, pListener);
    }

    @Override
    public void showOpenSettingNetwokingDialog(OnNetworkDialogListener pListener) {
        hideLoading();
        DialogManager.showMultiButtonDialog(getActivity(), SweetAlertDialog.NO_INTERNET, -1,
                getString(R.string.sdk_dialog_nointernet_content), pIndex -> {
                    if (pIndex == 0 && pListener != null) {
                        pListener.onCloseNetworkingDialog();
                    } else if (pIndex == 1) {
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                        if (pListener != null) {
                            pListener.onOpenSettingDialogClicked();
                        }
                    }
                },
                getResources().getString(R.string.dialog_turn_off),
                getResources().getString(R.string.dialog_turn_on));
    }

    @Override
    public void showConfirmDialog(String pMessage, String pButtonLeftText, String pButtonRightText, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showConfirmDialog(getActivity(),
                pMessage, pButtonLeftText, pButtonRightText, pListener);
    }

    @Override
    public void showSnackBar(String pMessage, String pActionMessage, int pDuration, OnSnackbarListener pOnCloseListener) {
        PaymentSnackBar.getInstance().dismiss();
        try {
            PaymentSnackBar.getInstance().setRootView(findViewById(R.id.supperRootView))
                    .setBgColor(getResources().getColor(R.color.yellow_bg_popup_error))
                    .setMessage(pMessage)
                    .setActionMessage(pActionMessage)
                    .setDuration(pDuration)
                    .setOnCloseListener(pOnCloseListener)
                    .show();
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void visibleCardNumberInput(boolean pVisible) {
        setVisible(R.id.card_field_container_pager, pVisible);
    }

    public void visibleCardInfo(boolean pIsVisible) {
        setVisible(R.id.zpw_card_info, pIsVisible);
    }

    public void visibleSubmitButton(boolean pIsVisible) {
        setVisible(R.id.zpw_submit_view, pIsVisible);
    }

    public void visibleBIDVAccountRegisterBtn(boolean visible) {
        setVisible(R.id.bidv_register_linearlayout, visible);
    }

    public void visibleCardViewNavigateButton(boolean pIsVisible) {
        setVisible(R.id.zpw_switch_card_button, pIsVisible);
    }

    public void visibleInputCardView(boolean pIsVisible) {
        setVisible(R.id.localcard_view_root, pIsVisible);
    }

    public void visibleWebView(boolean pIsVisible) {
        setVisible(R.id.zpw_threesecurity_webview, pIsVisible);
    }

    public void showMessageSnackBar(View pRootView, String pMessage, String pActionMessage, int pDuration, OnSnackbarListener pOnCloseListener) {
        PaymentSnackBar.getInstance().dismiss();
        try {
            PaymentSnackBar.getInstance().setRootView(pRootView)
                    .setBgColor(getResources().getColor(R.color.yellow_bg_popup_error))
                    .setMessage(pMessage)
                    .setActionMessage(pActionMessage)
                    .setDuration(pDuration)
                    .setOnCloseListener(pOnCloseListener)
                    .show();

        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void showDialogWarningLinkCardAndResetCardNumber() {
        showInfoDialog(getResources().getString(R.string.sdk_error_linkcard_not_support_mess), () -> {
            try {
                mPresenter.resetCardNumberAndShowKeyBoard();
            } catch (Exception e) {
                Timber.w(e);
            }
        });
    }

    public void showMenuItem() {
        mShowMenuItem = true;
        ActivityCompat.invalidateOptionsMenu(getActivity());
    }
}
