package vn.com.zalopay.wallet.ui.channellist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import java.util.List;

import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.helper.FontHelper;
import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.BaseFragment;
import vn.com.zalopay.wallet.ui.GenericFragment;
import vn.com.zalopay.wallet.view.adapter.RecyclerTouchListener;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;

import static vn.com.zalopay.wallet.helper.FontHelper.applyFont;
import static vn.com.zalopay.wallet.helper.RenderHelper.genDynamicItemDetail;

/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelListFragment extends GenericFragment<ChannelListPresenter> implements ChannelListContract.IView {
    private boolean delayClick = false;
    private View.OnClickListener mConfirmClick = view -> {
        if (!delayClick) {
            delayClick = true;
            new Handler().postDelayed(() -> delayClick = false, 1000);
            mPresenter.startPayment();
        }
    };
    private RecyclerView channel_list_recycler;
    private String mOriginTitle;

    private View order_amount_linearlayout;
    private TextView order_amount_txt;
    private TextView order_description_txt;
    private View appname_relativelayout;
    private TextView appname_txt;
    private TextView order_fee_txt;
    private LinearLayout item_detail_linearlayout;
    private Button confirm_button;
    private View view_top_linearlayout;
    private TextView toolbar_title;

    public static BaseFragment newInstance() {
        return new ChannelListFragment();
    }

    public static BaseFragment newInstance(Bundle args) {
        BaseFragment fragment = newInstance();
        if(args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    /***
     * apply font bold for some view
     */
    private void makeFont() {
        applyFont(confirm_button, GlobalData.getStringResource(RS.string.zpw_font_regular));
        applyFont(order_amount_txt, GlobalData.getStringResource(RS.string.zpw_font_medium));
        applyFont(toolbar_title, GlobalData.getStringResource(RS.string.zpw_font_medium));
    }

    @Override
    public boolean onBackPressed() {
        boolean back_pressed = mPresenter.onBackPressed();
        if (!back_pressed) {
            showQuitConfirm();
            back_pressed = true;
        }
        return back_pressed;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_channellist;
    }

    @Override
    protected void onViewBound(View view) {
        super.onViewBound(view);
        toolbar_title = (TextView) view.findViewById(R.id.toolbar_title);
        view_top_linearlayout = view.findViewById(R.id.view_top_linearlayout);

        order_amount_linearlayout = view.findViewById(R.id.order_amount_total_linearlayout);
        order_amount_txt = (TextView) view.findViewById(R.id.order_amount_total_txt);
        order_description_txt = (TextView) view.findViewById(R.id.order_description_txt);
        appname_relativelayout = view.findViewById(R.id.appname_relativelayout);
        appname_txt = (TextView) view.findViewById(R.id.appname_txt);
        order_fee_txt = (TextView) view.findViewById(R.id.order_fee_txt);
        item_detail_linearlayout = (LinearLayout) view.findViewById(R.id.item_detail_linearlayout);

        confirm_button = (Button) view.findViewById(R.id.confirm_button);
        confirm_button.setOnClickListener(mConfirmClick);

        channel_list_recycler = (RecyclerView) view.findViewById(R.id.channel_list_recycler);
        setupRecyclerView();

        makeFont();
    }

    @Override
    public void setTitle(String title) {
        if (TextUtils.isEmpty(mOriginTitle)) {
            mOriginTitle = title;
        }
        if (getActivity() != null) {
            ((ChannelListActivity)getActivity()).setToolbarTitle(title);
        }
    }

    protected void setupRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        //channel_list_recycler.setHasFixedSize(true);
        channel_list_recycler.setLayoutManager(mLayoutManager);
        channel_list_recycler.setItemAnimator(new DefaultItemAnimator());
        channel_list_recycler.addOnItemTouchListener(new RecyclerTouchListener(getContext(), channel_list_recycler));
    }

    @Override
    protected ChannelListPresenter initializePresenter() {
        return new ChannelListPresenter();
    }

    @Override
    protected void onDataBound(View view) {
        mPresenter.onPaymentReady();
    }

    @Override
    public void showLoading(String pTitle) {
        if (getActivity() != null) {
            getActivity().setTitle(pTitle);
            DialogManager.showProcessDialog(getActivity(), () -> showError(getResources().getString(R.string.zingpaysdk_alert_network_error)));
        }
    }

    @Override
    public void updateDefaultTitle() {
        if (getActivity() != null) {
            getActivity().setTitle(mOriginTitle);
        }
    }

    @Override
    public void hideLoading() {
        DialogManager.closeProcessDialog();
        updateDefaultTitle();
    }

    @Override
    public void showError(String pMessage) {
        hideLoading();
        DialogManager.showSweetDialogCustom(getActivity(), pMessage, getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.WARNING_TYPE, this::callbackThenTerminate);
    }

    @Override
    public void terminate() {
        Log.d(this, "recycle activity");
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void callbackThenTerminate() {
        if (mPresenter != null) {
            mPresenter.callback();
        }
        terminate();
    }

    @Override
    public void renderDynamicItemDetail(List<NameValuePair> nameValuePairList) {
        List<View> views = genDynamicItemDetail(getContext(), nameValuePairList);
        boolean hasView = views != null && views.size() > 0;
        if (hasView) {
            for (View view : views) {
                item_detail_linearlayout.addView(view);
            }
        }
        item_detail_linearlayout.setVisibility(hasView ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderAppInfo(String appName) {
        boolean hasAppName = !TextUtils.isEmpty(appName);
        if (hasAppName) {
            appname_txt.setText(appName);
        }
        appname_relativelayout.setVisibility(hasAppName ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderTotalAmountAndFee(double total_amount, double fee) {
        if (fee > 0) {
            String txtFee = String.format(getString(R.string.sdk_fee_format), StringUtil.formatVnCurrence(String.valueOf(fee)));
            order_fee_txt.setText(txtFee);
        } else {
            order_fee_txt.setText(getResources().getString(R.string.sdk_order_fee_free));
        }
        //order amount
        boolean hasAmount = total_amount > 0;
        if (hasAmount) {
            String txtAmount = StringUtil.formatVnCurrence(String.valueOf(total_amount));
            order_amount_txt.setText(txtAmount);
            order_amount_txt.setTextSize(getResources().getDimension(FontHelper.getFontSizeAmount(total_amount)));
        }
        order_amount_linearlayout.setVisibility(hasAmount ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderOrderInfo(AbstractOrder order) {
        if (order == null) {
            Log.d(this, "order is null - skip render order info");
            return;
        }
        //order desc
        boolean hasDesc = !TextUtils.isEmpty(order.description);
        if (hasDesc) {
            order_description_txt.setText(order.description);
        }
        order_description_txt.setVisibility(hasDesc ? View.VISIBLE : View.GONE);
        //order amount
        order.amount_total = order.amount + order.fee;
        renderTotalAmountAndFee(order.amount_total, order.fee);
    }

    @Override
    public void onBindingChannel(ChannelListAdapter pChannelAdapter) {
        channel_list_recycler.setAdapter(pChannelAdapter);
    }

    @Override
    public void showAppInfoNotFoundDialog() {
        DialogManager.showSweetDialogCustom(getActivity(),
                getResources().getString(R.string.zpw_not_allow_payment_app),
                getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.WARNING_TYPE, this::callbackThenTerminate);
    }

    @Override
    public void showForceUpdateLevelDialog() {
        DialogManager.showSweetDialogOptionNotice(getActivity(),
                getResources().getString(R.string.zpw_string_alert_profilelevel_update),
                getResources().getString(R.string.dialog_upgrade_button),
                getResources().getString(R.string.dialog_cancel_button), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        callbackThenTerminate();
                    }

                    @Override
                    public void onOKevent() {
                        mPresenter.setPaymentStatusAndCallback(PaymentStatus.LEVEL_UPGRADE_PASSWORD);
                        callbackThenTerminate();
                    }
                });
    }

    @Override
    public void showUpdateLevelDialog(String message, String btnCloseText, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showSweetDialogOptionNotice(getActivity(),
                message,
                getResources().getString(R.string.dialog_upgrade_button),
                btnCloseText, pListener);
    }

    @Override
    public void showWarningLinkCardBeforeWithdraw() {
        hideLoading();
        DialogManager.showSweetDialogOptionNotice(getActivity(),
                getResources().getString(R.string.zpw_string_alert_linkcard_channel_withdraw),
                getResources().getString(R.string.dialog_linkcard_button),
                getResources().getString(R.string.dialog_close_button),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        callbackThenTerminate();
                    }

                    @Override
                    public void onOKevent() {
                        mPresenter.setPaymentStatusAndCallback(PaymentStatus.DIRECT_LINKCARD);
                        callbackThenTerminate();
                    }
                });
    }

    @Override
    public void showOpenSettingNetwokingDialog(ZPWPaymentOpenNetworkingDialogListener pListener) {
        hideLoading();
        DialogManager.showSweetDialog(getActivity(), SweetAlertDialog.NO_INTERNET,
                getString(R.string.zingpaysdk_alert_title_nointernet),
                getString(R.string.zingpaysdk_alert_content_nointernet), pIndex -> {
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
    public void showQuitConfirm() {
        String mess = mPresenter.getQuitMessage();
        DialogManager.showSweetDialogConfirm(getActivity(), mess,
                getString(R.string.dialog_khong_button),
                getString(R.string.dialog_co_button), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        mPresenter.setPaymentStatusAndCallback(PaymentStatus.USER_CLOSE);
                        terminate();
                    }

                    @Override
                    public void onOKevent() {
                    }
                });
    }

    @Override
    public void showInfoDialog(String pMessage) {
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
    public void showSupportBankVersionDialog(String pMessage) {
        DialogManager.showSweetDialogConfirm(getActivity(),
                pMessage, getResources().getString(R.string.dialog_upgrade_button),
                getResources().getString(R.string.dialog_choose_again_button), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKevent() {
                        PlayStoreUtils.openPlayStoreForUpdate(GlobalData.getMerchantActivity(), BuildConfig.PACKAGE_IN_PLAY_STORE, "Zalo Pay", "force-app-update", "bank-future");
                        terminate();
                    }
                });
    }

    @Override
    public void showRetryDialog(String pMessage, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showSweetDialogRetry(getActivity(), pMessage, pListener);
    }

    @Override
    public void showSnackBar(String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener) {
        PaymentSnackBar.getInstance().dismiss();
        try {
            PaymentSnackBar.getInstance().setRootView(view_top_linearlayout)
                    .setBgColor(GlobalData.getAppContext().getResources().getColor(R.color.yellow_bg_popup_error))
                    .setMessage(pMessage)
                    .setActionMessage(pActionMessage)
                    .setDuration(pDuration)
                    .setOnCloseListener(pOnCloseListener)
                    .show();
        } catch (Exception e) {
            Log.d(this, e);
        }
    }

    @Override
    public void enableConfirmButton(int buttonTextId, int bgResourceId) {
        confirm_button.setEnabled(true);
        if (buttonTextId != -1) {
            confirm_button.setText(getString(buttonTextId));
        }
        if (bgResourceId != -1) {
            confirm_button.setBackgroundResource(bgResourceId);
        }
    }

    @Override
    public void disableConfirmButton() {
        confirm_button.setEnabled(false);
        confirm_button.setBackgroundResource(R.drawable.bg_btn_silver_border);
    }
}
