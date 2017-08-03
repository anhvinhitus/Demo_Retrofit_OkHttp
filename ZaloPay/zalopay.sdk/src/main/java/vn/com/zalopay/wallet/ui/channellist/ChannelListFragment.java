package vn.com.zalopay.wallet.ui.channellist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zalopay.ui.widget.UIBottomSheetDialog;
import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.analytics.ZPScreens;
import vn.com.zalopay.utility.CurrencyUtil;
import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.helper.FontHelper;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.listener.onNetworkingDialogCloseListener;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.ui.BaseActivity;
import vn.com.zalopay.wallet.ui.BaseFragment;
import vn.com.zalopay.wallet.ui.GenericFragment;
import vn.com.zalopay.wallet.view.adapter.RecyclerTouchListener;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;
import vn.com.zalopay.wallet.voucher.IInteractVoucher;
import vn.com.zalopay.wallet.voucher.IVoucherDialogBuilder;
import vn.com.zalopay.wallet.voucher.VoucherRender;

import static vn.com.zalopay.wallet.helper.FontHelper.applyFont;
import static vn.com.zalopay.wallet.helper.RenderHelper.genDynamicItemDetail;

/*
 * Created by chucvv on 6/12/17.
 */

public class ChannelListFragment extends GenericFragment<ChannelListPresenter> implements ChannelListContract.IView {
    IVoucherDialogBuilder mVoucherDialogBuilder;
    VoucherRender mVoucherRender;
    UIBottomSheetDialog mVoucherDialog;
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
    private View voucher_relativelayout;
    private View voucher_txt;
    private View active_voucher_relativelayout;
    private TextView active_voucher_textview;
    private TextView voucher_discount_amount_textview;
    private View active_voucher_del_img;

    public static BaseFragment newInstance() {
        return new ChannelListFragment();
    }

    public static BaseFragment newInstance(Bundle args) {
        BaseFragment fragment = newInstance();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    /***
     * apply font bold for some view
     */
    private void makeFont() {
        applyFont(order_amount_txt, GlobalData.getStringResource(RS.string.sdk_font_medium));
    }

    @Override
    protected String getTrackingScreenName() {
        return ZPScreens.PAYMENT_METHOD;
    }

    @Override
    public boolean onBackPressed() {
        if (mPresenter.quitWithoutConfirm()) {
            return false;
        }
        if (!mPresenter.onBackPressed()) {
            showQuitConfirm();
        }
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_channellist;
    }

    @Override
    protected void onViewBound(View view) {
        super.onViewBound(view);
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

        voucher_relativelayout = view.findViewById(R.id.voucher_relativelayout);
        voucher_txt = view.findViewById(R.id.voucher_txt);

        active_voucher_relativelayout = view.findViewById(R.id.active_voucher_relativelayout);
        active_voucher_textview = (TextView) view.findViewById(R.id.active_voucher_textview);
        voucher_discount_amount_textview = (TextView) view.findViewById(R.id.voucher_discount_amount_textview);
        active_voucher_del_img = view.findViewById(R.id.active_voucher_del_img);

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
            ((ChannelListActivity) getActivity()).setToolbarTitle(title);
        }
        Timber.d("set title %s", title);
    }

    private boolean showingVoucherDialog() {
        return mVoucherDialog != null && mVoucherDialog.isShowing();
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
        mPresenter.trackEventLaunch();
    }

    @Override
    public void showLoading(String pTitle) {
        if (getActivity() != null) {
            setTitle(pTitle);
            DialogManager.showProcessDialog(getActivity(), () -> showError(getResources().getString(R.string.sdk_loading_timeout)));
        }
    }

    @Override
    public void updateDefaultTitle() {
        if (getActivity() != null) {
            getActivity().setTitle(mOriginTitle);
            Timber.d("set default title %s", mOriginTitle);
        }
    }

    @Override
    public void hideLoading() {
        DialogManager.closeLoadDialog();
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
        Timber.d("recycle activity");
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showingVoucherDialog()
                && mVoucherRender != null) {
            mVoucherRender.showKeyBoard();
            Timber.d("show keyboard voucher dialog on resume");
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
    public ChannelListAdapter initChannelListAdapter(long amount, UserInfo userInfo, int userLevel, int transtype) {
        ChannelListAdapter channelAdapter = new ChannelListAdapter();
        Context context = getContext();
        channelAdapter.addZaloPayBinder(context, amount, userInfo.balance, transtype);
        channelAdapter.addMapBinder(context, amount);
        channelAdapter.addTitle();
        channelAdapter.addInputBinder(context, amount, transtype);
        onBindingChannel(channelAdapter);

        return channelAdapter;
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
    public void renderVoucher() {
        voucher_relativelayout.setVisibility(View.VISIBLE);
        voucher_txt.setOnClickListener(view -> {
            try {
                showVoucherCodeInput();
            } catch (Exception e) {
                Timber.d(e);
            }
        });
        active_voucher_relativelayout.setVisibility(View.GONE);
    }

    @Override
    public void renderActiveVoucher(String voucherCode, double discountAmount) {
        active_voucher_relativelayout.setVisibility(View.VISIBLE);

        String codeformat = getResources().getString(R.string.sdk_active_voucher_cod_format);
        voucherCode = String.format(codeformat, voucherCode);
        active_voucher_textview.setText(voucherCode);

        String discountFormat = getResources().getString(R.string.sdk_discount_amount_voucher_format);
        discountFormat = String.format(discountFormat, CurrencyUtil.formatCurrency(discountAmount));

        voucher_discount_amount_textview.setText(discountFormat);
        ResourceManager.loadImageIntoView(active_voucher_del_img, RS.drawable.ic_round_delete);
        active_voucher_del_img.setOnClickListener(view -> showConfirmDeleteVoucherDialog(new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {
                if (mPresenter == null) {
                    return;
                }
                mPresenter.clearVoucher();
                renderVoucher();
            }

            @Override
            public void onOKEvent() {
            }
        }));

        hideVoucherCodePopup();
    }

    @Override
    public void setVoucherError(String error) {
        if (mVoucherRender == null) {
            return;
        }
        mVoucherRender.setError(error);
        mVoucherRender.hideLoading();
    }

    @Override
    public void hideVoucherCodePopup() {
        if (mVoucherRender == null) {
            Timber.d("mVoucherRender is null - skip hide voucher code popup");
            return;
        }
        try {
            mVoucherRender.OnDismiss();
            mVoucherRender = null;
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void showVoucherCodeInput() throws Exception {
        mVoucherDialogBuilder = VoucherRender.getBuilder();
        View contentView = View.inflate(getContext(), R.layout.module__vouchercode__input, null);
        mVoucherDialogBuilder
                .setView(contentView)
                .setInteractListener(new IInteractVoucher() {
                    @Override
                    public void onClose() {
                        if (mVoucherDialogBuilder != null) {
                            mVoucherDialogBuilder.release();
                            mVoucherDialogBuilder = null;
                        }
                    }

                    @Override
                    public void onVoucherInfoComplete(String voucherCode) {
                        if (TextUtils.isEmpty(voucherCode)) {
                            return;
                        }
                        if (mPresenter != null) {
                            if (mVoucherRender != null) {
                                mVoucherRender.showLoading();
                            }
                            mPresenter.useVoucher(voucherCode);
                        }
                    }
                });
        mVoucherRender = (VoucherRender) mVoucherDialogBuilder.build();
        mVoucherDialog = new UIBottomSheetDialog(getActivity(), vn.zalopay.promotion.R.style.CoffeeDialog, mVoucherRender);
        mVoucherDialog.preventDrag(true);
        mVoucherDialog.setCanceledOnTouchOutside(false);
        mVoucherDialog.show();
        mVoucherDialog.setState(BottomSheetBehavior.STATE_EXPANDED);
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
    public void renderOrderAmount(double order_total_amount) {
        //order amount
        boolean hasAmount = order_total_amount > 0;
        if (hasAmount) {
            String txtAmount = CurrencyUtil.formatCurrency(order_total_amount, false);
            order_amount_txt.setText(txtAmount);
            order_amount_txt.setTextSize(getResources().getDimension(FontHelper.getFontSizeAmount(order_total_amount)));
        }
        order_amount_linearlayout.setVisibility(hasAmount ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderOrderFee(double order_fee) {
        if (order_fee > 0) {
            String txtFee = CurrencyUtil.formatCurrency(order_fee);
            order_fee_txt.setText(txtFee);
        } else {
            order_fee_txt.setText(getResources().getString(R.string.sdk_order_fee_free));
        }
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
            order_description_txt.setText(order.description);
        }
        order_description_txt.setVisibility(hasDesc ? View.VISIBLE : View.GONE);
        //order amount
        order.amount_total = order.amount + order.fee;
        renderOrderAmount(order.amount_total);
        renderOrderFee(order.fee);
    }

    @Override
    public void onBindingChannel(ChannelListAdapter pChannelAdapter) {
        channel_list_recycler.setAdapter(pChannelAdapter);
    }

    @Override
    public void showAppInfoNotFoundDialog() {
        DialogManager.showSweetDialogCustom(getActivity(),
                getResources().getString(R.string.sdk__app_not_allow_payment_mess),
                getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.WARNING_TYPE, this::callbackThenTerminate);
    }

    @Override
    public void showWarningLinkCardBeforeWithdraw() {
        hideLoading();
        DialogManager.showConfirmDialog(getActivity(),
                getResources().getString(R.string.dialog_title_normal),
                getResources().getString(R.string.sdk_withdraw_no_link_warning_mess),
                getResources().getString(R.string.dialog_linkcard_button),
                getResources().getString(R.string.dialog_close_button),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        callbackThenTerminate();
                    }

                    @Override
                    public void onOKEvent() {
                        mPresenter.setPaymentStatusAndCallback(PaymentStatus.DIRECT_LINKCARD);
                        callbackThenTerminate();
                    }
                });
    }

    @Override
    public void showOpenSettingNetwokingDialog(onNetworkingDialogCloseListener pListener) {
        hideLoading();
        DialogManager.showMultiButtonDialog(getActivity(), SweetAlertDialog.NO_INTERNET, -1,
                getString(R.string.sdk_dialog_nointernet_title),
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
    public void showConfirmDeleteVoucherDialog(ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showConfirmDialog(getActivity(),
                null,
                getString(R.string.sdk_delete_voucher_confirm_text),
                getString(R.string.dialog_khong_button),
                getString(R.string.dialog_co_button), pListener);
    }

    @Override
    public void showQuitConfirm() {
        String mess = mPresenter.getQuitMessage();
        if (TextUtils.isEmpty(mess)) {
            terminate();
        }
        DialogManager.showConfirmDialog(getActivity(),
                getString(R.string.dialog_title_confirm),
                mess,
                getString(R.string.dialog_khong_button),
                getString(R.string.dialog_co_button), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        mPresenter.setPaymentStatusAndCallback(PaymentStatus.USER_CLOSE);
                        terminate();
                    }

                    @Override
                    public void onOKEvent() {
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
        DialogManager.showConfirmDialog(getActivity(),
                getString(com.zalopay.ui.widget.R.string.dialog_title_confirm),
                pMessage, getResources().getString(R.string.dialog_upgrade_button),
                getResources().getString(R.string.dialog_choose_again_button), new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {

                    }

                    @Override
                    public void onOKEvent() {
                        PlayStoreUtils.openPlayStoreForUpdate(GlobalData.getMerchantActivity(), BuildConfig.PACKAGE_IN_PLAY_STORE, "Zalo Pay", "force-app-update", "bank-future");
                        terminate();
                    }
                });
    }

    @Override
    public void showRetryDialog(String pMessage, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showRetryDialog(getActivity(), pMessage, pListener);
    }

    @Override
    public void showSnackBar(String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener) {
        PaymentSnackBar.getInstance().dismiss();
        try {
            PaymentSnackBar.getInstance().setRootView(view_top_linearlayout)
                    .setBgColor(getResources().getColor(R.color.yellow_bg_popup_error))
                    .setMessage(pMessage)
                    .setActionMessage(pActionMessage)
                    .setDuration(pDuration)
                    .setOnCloseListener(pOnCloseListener)
                    .show();
        } catch (Exception e) {
            Timber.d(e, "show snack bar exception");
        }
    }

    @Override
    public void enablePaymentButton(int buttonTextId, int bgResourceId) {
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

    @Override
    public void scrollToPos(int position) {
        channel_list_recycler.scrollToPosition(position);
    }

    @Override
    public void switchToResultScreen(StatusResponse pResponse, boolean pShouldShowFingerPrintToast) throws Exception {
        if (!(getActivity() instanceof BaseActivity) || getActivity().isFinishing()) {
            throw new IllegalStateException("Activity is finish");
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.STATUS_RESPONSE, pResponse);
        bundle.putBoolean(Constants.SHOWFFTOAST, pShouldShowFingerPrintToast);
        BaseFragment fragment = ResultPaymentFragment.newInstance(bundle);
        ((BaseActivity) getActivity()).hostFragment(fragment);
    }
}
