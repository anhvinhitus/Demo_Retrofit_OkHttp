package vn.com.zalopay.wallet.ui.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zalopay.ui.widget.dialog.DialogManager;
import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventDialogListener;
import com.zalopay.ui.widget.dialog.listener.ZPWOnSweetDialogListener;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import vn.com.vng.zalopay.data.util.NameValuePair;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.utility.StringUtil;
import vn.com.zalopay.utility.ViewUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.ESuggestActionType;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.helper.FontHelper;
import vn.com.zalopay.wallet.listener.ZPWPaymentOpenNetworkingDialogListener;
import vn.com.zalopay.wallet.listener.onCloseSnackBar;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.ui.BaseFragment;
import vn.com.zalopay.wallet.view.custom.PaymentSnackBar;

import static vn.com.zalopay.wallet.helper.FontHelper.applyFont;
import static vn.com.zalopay.wallet.helper.RenderHelper.genDynamicItemDetail;

/**
 * Created by chucvv on 6/12/17.
 */

public class ChannelFragment extends RenderFragment<ChannelPresenter> implements ChannelContract.IView {

    private final View.OnClickListener updateInfoClick = v -> {
        mPresenter.setPaymentStatusAndCallback(PaymentStatus.LEVEL_UPGRADE_CMND_EMAIL);
    };
    @LayoutRes
    int mLayoutId = R.layout.screen__card;
    private String mOriginTitle;
    private boolean allowClick = true;
    private boolean visualSupportView = false;
    private Bundle mData;
    private View.OnClickListener onSubmitClick = view -> {
        if (allowClick) {
            allowClick = false;
            mPresenter.onSubmitClick();
            new Handler().postDelayed(() -> allowClick = true, 3000);
        }
    };
    private View.OnClickListener itemSupportButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                int i = view.getId();
                if (i == R.id.question_button) {
                    startCenterSupport();
                } else if (i == R.id.support_button) {
                    mPresenter.startSupportScreen();
                }
                closeSupportView();
            } catch (Exception ex) {
                Log.e(this, ex);
            }
        }
    };
    private View.OnClickListener supportClick = view -> showSupportView();

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

    public ChannelPresenter sharePresenter() {
        return mPresenter;
    }

    private void startCenterSupport() {
        Intent intent = new Intent();
        intent.setAction(Constants.SUPPORT_INTRO_ACTION_SUPPORT_CENTER);
        startActivity(intent);
    }

    public void onUserInteraction() {
        mPresenter.onUserInteraction();
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
        return mPresenter.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public int getLayoutId() {
        return mLayoutId;
    }

    @Override
    protected void onViewBound(View view) {
        super.onViewBound(view);
        mRootView = view;
        findViewById(R.id.zpsdk_btn_submit).setOnClickListener(onSubmitClick);
        findViewById(R.id.zpw_payment_fail_rl_support).setOnClickListener(supportClick);
        findViewById(R.id.zpw_payment_fail_rl_update_info).setOnClickListener(updateInfoClick);
        mPresenter.pushArgument(mData);

        makeFont();
    }

    /***
     * apply font bold for some view
     */
    private void makeFont() {
        applyFont(findViewById(R.id.zpsdk_btn_submit), GlobalData.getStringResource(RS.string.zpw_font_regular));
        applyFont(findViewById(R.id.order_amount_total_txt), GlobalData.getStringResource(RS.string.zpw_font_medium));
    }

    @Override
    public void setTitle(String title) {
        if (TextUtils.isEmpty(mOriginTitle)) {
            mOriginTitle = title;
        }
        if (getActivity() != null) {
            ((ChannelActivity) getActivity()).setToolbarTitle(title);
        }
    }

    @Override
    protected ChannelPresenter initializePresenter() {
        return new ChannelPresenter();
    }

    @Override
    protected void onDataBound(View view) {
        mPresenter.startPayment();
    }

    @Override
    public void showLoading(String pTitle) {
        if (getActivity() != null) {
            getActivity().setTitle(pTitle);
            DialogManager.showProcessDialog(getActivity(), () -> showError(getResources().getString(R.string.zingpaysdk_alert_network_error)));
        }
    }

    @Override
    public void hideLoading() {
        DialogManager.closeProcessDialog();
        if (getActivity() != null) {
            getActivity().setTitle(mOriginTitle);
        }
    }

    @Override
    public void showError(String pMessage) {
        hideLoading();
        DialogManager.showSweetDialogCustom(getActivity(), pMessage, getResources().getString(R.string.dialog_close_button),
                SweetAlertDialog.WARNING_TYPE, () -> callbackThenTerminate());
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
            try {
                mPresenter.setCallBack(Activity.RESULT_OK);
            } catch (Exception e) {
                Log.e(this, e);
            }
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
        DialogManager.showSweetDialogConfirm(getActivity(), message,
                getString(R.string.dialog_khong_button),
                getString(R.string.dialog_co_button), pListener);
    }

    @Override
    public void showDialogManyOption(ZPWOnSweetDialogListener pListener) {
        DialogManager.showDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE, null,
                GlobalData.getStringResource(RS.string.zpw_confirm_quit_loadsite), pListener,
                GlobalData.getStringResource(RS.string.dialog_khong_button),
                GlobalData.getStringResource(RS.string.dialog_co_button),
                GlobalData.getStringResource(RS.string.dialog_getstatus_button));
    }

    @Override
    public void showUpdateLevelDialog(String message, String btnCloseText, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showSweetDialogOptionNotice(getActivity(),
                message,
                getResources().getString(R.string.dialog_upgrade_button),
                btnCloseText, pListener);
    }

    /***
     * Show support view
     */
    private void showSupportView() {
        try {
            visualSupportView = true;
            setVisible(R.id.zpw_pay_support_buttom_view, true);
            findViewById(R.id.zpw_pay_support_buttom_view).setOnClickListener(itemSupportButtonClick);
            findViewById(R.id.question_button).setOnClickListener(itemSupportButtonClick);
            findViewById(R.id.support_button).setOnClickListener(itemSupportButtonClick);
            findViewById(R.id.cancel_spview_button).setOnClickListener(itemSupportButtonClick);
            View v = findViewById(R.id.layout_spview_animation);
            if (v != null) {
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
                v.startAnimation(hyperspaceJumpAnimation);
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Override
    public void closeSupportView() {
        View v = findViewById(R.id.layout_spview_animation);
        if (v != null) {
            Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
            v.startAnimation(hyperspaceJumpAnimation);
        }
        visualSupportView = false;
        final Handler handler = new Handler();
        handler.postDelayed(() -> setVisible(R.id.zpw_pay_support_buttom_view, false), 300);
    }

    @Override
    public boolean visualSupportView() {
        return visualSupportView;
    }

    @Override
    public String getFailMess() {
        TextView textView = (TextView) findViewById(R.id.sdk_trans_fail_reason_message_textview);
        return ((textView != null) ? textView.getText().toString() : "");
    }

    @Override
    public void renderAppInfo(String appName) {
        boolean hasAppName = !TextUtils.isEmpty(appName);
        if (hasAppName) {
            setText(R.id.appname_txt, appName);
        }
        setVisible(R.id.appname_relativelayout, hasAppName);
    }

    @Override
    public void renderTotalAmountAndFee(double total_amount, double fee) {
        if (fee > 0) {
            String txtFee = StringUtil.formatVnCurrence(String.valueOf(fee));
            setText(R.id.order_fee_txt, txtFee);
        } else {
            setText(R.id.order_fee_txt, getResources().getString(R.string.sdk_order_fee_free));
        }
        //order amount
        boolean hasAmount = total_amount > 0;
        if (hasAmount) {
            String order_amount = StringUtil.formatVnCurrence(String.valueOf(total_amount));
            setText(R.id.order_amount_total_txt, order_amount);
            ((TextView) findViewById(R.id.order_amount_total_txt)).setTextSize(getResources().getDimension(FontHelper.getFontSizeAmount(total_amount)));
        }
        setVisible(R.id.order_amount_total_linearlayout, hasAmount);
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
            setText(R.id.order_description_txt, order.description);
        }
        setVisible(R.id.order_description_txt, hasDesc);
        //order amount
        order.amount_total = order.amount + order.fee;
        renderTotalAmountAndFee(order.amount_total, order.fee);
    }

    @Override
    public void renderByResource(String screenName) {
        try {
            renderByResource(screenName, null, null);
        } catch (Exception e) {
            Log.e(this, e);
            showError(getString(R.string.zpw_string_error_layout));
        }
    }

    @Override
    public void renderByResource(String screenName, DStaticViewGroup pAdditionStaticViewGroup, DDynamicViewGroup pAdditionDynamicViewGroup) {
        try {
            Log.d(this, "start render screen name", screenName);
            long time = System.currentTimeMillis();
            ResourceManager resourceManager = ResourceManager.getInstance(screenName);
            if (resourceManager != null) {
                mResourceRender = resourceManager.produceRendering(this);
                if (mResourceRender != null) {
                    mResourceRender.render();
                    mResourceRender.render(pAdditionStaticViewGroup, pAdditionDynamicViewGroup);
                } else {
                    Log.d(this, "resource render is null");
                }
            } else {
                Log.d(this, "resource manager is null");
            }
            Log.d(this, "render resource: Total time:", (System.currentTimeMillis() - time));
        } catch (Exception e) {
            Log.e(this, e);
            showError(getString(R.string.zpw_string_error_layout));
        }
    }

    @Override
    public void renderResourceAfterDelay(String screenName) {
        final View buttonWrapper = findViewById(R.id.zpw_switch_card_button);
        if (buttonWrapper != null) {
            ViewTreeObserver vto = buttonWrapper.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver obs = buttonWrapper.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);
                    renderByResource(screenName);
                }
            });
        } else {
            Log.d(this, "reader resource after delaying 500ms");
            new Handler().postDelayed(() -> renderByResource(screenName), 500);
        }
    }

    @Override
    public void enableSubmitBtn() {
        View view = findViewById(R.id.zpsdk_btn_submit);
        if (view != null) {
            view.setEnabled(true);
        }
    }

    @Override
    public void changeBgSubmitButton(boolean finalStep) {
        View view = findViewById(R.id.zpsdk_btn_submit);
        if (view != null) {
            if (finalStep) {
                view.setBackgroundResource(R.drawable.bg_btn_green_border_selector);
            } else {
                view.setBackgroundResource(R.drawable.bg_btn_blue_border_selector);
            }
        }
    }

    @Override
    public void disableSubmitBtn() {
        View view = findViewById(R.id.zpsdk_btn_submit);
        if (view != null) {
            view.setEnabled(false);
            view.setBackgroundResource(R.drawable.zpw_bg_button_disable);
        }
    }

    @Override
    public void visiableOrderInfo(boolean visible) {
        setVisible(R.id.orderinfo_module, visible);
    }

    @Override
    public void updateCardNumberFont() {
        new Handler().postDelayed(() -> applyFont(findViewById(R.id.edittext_localcard_number), GlobalData.getStringResource(RS.string.zpw_font_medium)), 500);
    }

    @Override
    public void marginSubmitButtonTop(boolean viewEnd) {
        View submitButton = findViewById(R.id.zpw_submit_view);
        View authenLocalView = findViewById(R.id.linearlayout_selection_authen);
        View authenInputCardView = findViewById(R.id.linearlayout_authenticate_local_card);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int paddingButtom = (int) getResources().getDimension(R.dimen.zpw_margin_top_submit_button_phone);
        if (!SdkUtils.isTablet(getActivity())) {
            params.setMargins(0, (int) getResources().getDimension(R.dimen.zpw_margin_top_submit_button_phone), 0, 0);
            if (submitButton != null) {
                submitButton.setLayoutParams(params);
                submitButton.requestLayout();
            }
            if (authenLocalView != null) {
                authenLocalView.setPadding(0, 0, 0, paddingButtom);
                authenLocalView.requestLayout();
            }
            if (authenInputCardView != null) {
                authenInputCardView.setPadding(0, 0, 0, paddingButtom);
                authenInputCardView.requestLayout();
            }
            Log.d(this, "setMarginSubmitButtonTop  Phone");
        } else {
            if (submitButton != null) {
                if (viewEnd)
                    params.setMargins(0, (int) getResources().getDimension(R.dimen.zpw_margin_top_submit_button_phone), 0, 0);
                else
                    params.setMargins(0, (int) getResources().getDimension(R.dimen.zpw_margin_top_submit_button_tab), 0, 0);
                submitButton.setLayoutParams(params);
                submitButton.requestLayout();
            }
            Log.d(this, "setMarginSubmitButtonTop  Tab");
        }
    }

    public void renderDynamicItemDetail(View viewContainer, List<NameValuePair> nameValuePairList) throws Exception {
        List<View> views = genDynamicItemDetail(getContext(), nameValuePairList);
        boolean hasView = views != null && views.size() > 0;
        LinearLayout stubView = (LinearLayout) viewContainer.findViewById(R.id.item_detail_linearlayout);
        if (hasView && stubView != null) {
            for (View view : views) {
                stubView.addView(view);
            }
        }
        setVisible(R.id.item_detail_linearlayout, hasView);
    }

    @Override
    public void overrideFont() {
        FontHelper.overrideFont((ViewGroup) ((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0));
    }

    @Override
    public void showMaintenanceServiceDialog(String message) {
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.zpw_string_alert_maintenance);
        }
        showInfoDialog(message, () -> mPresenter.setPaymentStatusAndCallback(PaymentStatus.SERVICE_MAINTENANCE));
    }

    @Override
    public void showRetryDialog(String pMessage, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showSweetDialogRetry(getActivity(), pMessage, pListener);
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
    public void showConfirmDialog(String pMessage, String pButtonLeftText, String pButtonRightText, ZPWOnEventConfirmDialogListener pListener) {
        DialogManager.showSweetDialogConfirm(getActivity(), pMessage, pButtonLeftText, pButtonRightText, pListener);
    }

    @Override
    public void showSnackBar(String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener) {
        PaymentSnackBar.getInstance().dismiss();
        try {
            PaymentSnackBar.getInstance().setRootView(findViewById(R.id.supperRootView))
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

    public void visibleCardInfo(boolean pIsVisible) {
        setVisible(R.id.zpw_card_info, pIsVisible);
    }

    public void visibleSubmitButton(boolean pIsVisible) {
        setVisible(R.id.zpw_submit_view, pIsVisible);
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

    private void renderTransDetail(View viewContainer, boolean isLink, String pTransID, AbstractOrder order, String appName, boolean visibleTrans) throws Exception {
        //service name
        boolean hasAppName = !TextUtils.isEmpty(appName);
        TextView appname_txt = (TextView) viewContainer.findViewById(R.id.appname_txt);
        if (hasAppName) {
            appname_txt.setText(appName);
        }
        appname_txt.setVisibility(hasAppName ? View.VISIBLE : View.GONE);
        //trans id
        boolean hasTransId = !TextUtils.isEmpty(pTransID) && Long.parseLong(pTransID) > 0;
        TextView transaction_id_txt = (TextView) viewContainer.findViewById(R.id.transaction_id_txt);
        if (hasTransId) {
            transaction_id_txt.setText(pTransID);
        } else {
            transaction_id_txt.setText(getResources().getString(R.string.sdk_no_transid_label));
        }
        View sdk_trans_id_relativelayout = viewContainer.findViewById(R.id.sdk_trans_id_relativelayout);
        sdk_trans_id_relativelayout.setVisibility(visibleTrans ? View.VISIBLE : View.GONE);//hide trans id if unlink account
        //trans time
        Long paymentTime = order != null ? order.apptime : new Date().getTime();
        TextView transaction_time_txt = (TextView) viewContainer.findViewById(R.id.transaction_time_txt);
        transaction_time_txt.setText(SdkUtils.convertDateTime(paymentTime));
        //trans fee
        String transFee = order != null && order.fee > 0 ? String.format(getResources().getString(R.string.sdk_fee_format), StringUtil.formatVnCurrence(String.valueOf(order.fee))) :
                getResources().getString(R.string.sdk_order_fee_free);
        TextView order_fee_txt = (TextView) viewContainer.findViewById(R.id.order_fee_txt);
        order_fee_txt.setText(transFee);
        //render item detail dynamic
        if (order != null) {
            List<NameValuePair> items = order.parseItems();
            renderDynamicItemDetail(viewContainer, items);
        }

        if (isLink) {
            setVisible(R.id.appname_result_relativelayout, false);
            setVisible(R.id.fee_relativelayout, false);
        }
    }

    public void renderSuccess(boolean isLink, String pTransID, UserInfo userInfo, AbstractOrder order, String appName, String descLinkAccount, boolean hideAmount,
                              boolean isTransfer, UserInfo destinationUser, String pToolbarTitle) {
        //transaction amount
        boolean hasAmount = order != null && order.amount_total > 0;
        if (hasAmount) {
            setTextHtml(R.id.success_order_amount_total_txt, StringUtil.formatVnCurrence(String.valueOf(order.amount_total)));
            ((TextView) findViewById(R.id.success_order_amount_total_txt)).setTextSize(getResources().getDimension(FontHelper.getFontSizeAmount(order.amount_total)));
        }
        if (!hasAmount || hideAmount) {
            setVisible(R.id.success_order_amount_total_linearlayout, false);
        }
        //desc
        String desc = descLinkAccount;
        if (TextUtils.isEmpty(desc)) {
            desc = order != null ? order.description : null;
        }
        boolean hasDesc = !TextUtils.isEmpty(desc);
        if (hasDesc) {
            setText(R.id.description_txt, desc);
        }
        setVisible(R.id.description_txt, hasDesc);
        //show 2 user avatar in tranfer money
        if (isTransfer) {
            //prevent capture screen
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            setVisible(R.id.money_tranfer_useravatar_linearlayout, true);
            if (destinationUser != null) {
                loadIntoView(R.id.img_avatarTo, destinationUser.avatar);
            }
            if (userInfo != null && !TextUtils.isEmpty(userInfo.avatar)) {
                loadIntoView(R.id.img_avatarFrom, userInfo.avatar);
            }
            loadIntoView(R.id.arrow_imageview, ResourceManager.getAbsoluteImagePath(RS.drawable.ic_arrow));
        }
        //inflat trans detail layout
        ViewStub success_trans_detail_stub = (ViewStub) findViewById(R.id.success_trans_detail_stub);
        if (success_trans_detail_stub != null) {
            View trans_detail_view = success_trans_detail_stub.inflate();
            try {
                renderTransDetail(trans_detail_view, isLink, pTransID, order, appName, true);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        //anim success icon
        ViewUtils.animIcon(getActivity(), R.id.success_imageview);
        changeSubmitButtonBackground();
        //update title
        setTitle(pToolbarTitle);
        updateToolBar();
        enableSubmitBtn();
    }

    public void renderFail(boolean isLink, String pMessage, String pTransID, AbstractOrder order, String appName, StatusResponse statusResponse,
                           boolean visibleTrans, String pToolBarTitle) {
        boolean hasTransFailMessage = !TextUtils.isEmpty(pMessage);
        if (hasTransFailMessage) {
            setText(R.id.sdk_trans_fail_reason_message_textview, pMessage);
        }
        setVisible(R.id.sdk_trans_fail_reason_message_textview, hasTransFailMessage);
        //inflate trans detail layout
        ViewStub fail_trans_detail_stub = (ViewStub) findViewById(R.id.fail_trans_detail_stub);
        if (fail_trans_detail_stub != null) {
            View trans_detail_view = fail_trans_detail_stub.inflate();
            try {
                renderTransDetail(trans_detail_view, isLink, pTransID, order, appName, visibleTrans);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        // The inform text would be set from server
        if (statusResponse != null) {
            //message action
            boolean hasSuggestActionMessage = !TextUtils.isEmpty(statusResponse.suggestmessage);
            if (hasSuggestActionMessage) {
                setText(R.id.sdk_sugguest_action_message_textview, statusResponse.suggestmessage);
            }
            setVisible(R.id.sdk_sugguest_action_message_textview, hasSuggestActionMessage);
            if (statusResponse.hasSuggestAction()) {
                setLayoutBasedOnSuggestActions(statusResponse.suggestaction);
            }
        }
        ViewUtils.animIcon(getActivity(), R.id.fail_imageview);
        changeSubmitButtonBackground();
        //update title
        setTitle(pToolBarTitle);
        updateToolBar();
        enableSubmitBtn();
    }

    private void updateToolBar() {
        ((ChannelActivity) getActivity()).hideDisplayHome();
        ((ChannelActivity) getActivity()).centerTitle();
    }

    public void showToast(int layout) {
        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(View.inflate(getContext(), layout, null));
        toast.show();
    }

    private void changeSubmitButtonBackground() {
        Button close_btn = (Button) findViewById(R.id.zpsdk_btn_submit);
        if (close_btn != null) {
            close_btn.setTextColor(ContextCompat.getColor(getContext(), R.color.text_color_grey));
            close_btn.setBackgroundResource(R.drawable.bg_btn_light_blue_border_selector);
        }
    }

    private void setLayoutBasedOnSuggestActions(int[] suggestActions) {
        // Define view to set view position based on suggest action from server response
        View rlUpdateInfo = findViewById(R.id.zpw_payment_fail_rl_update_info);
        View rlSupport = findViewById(R.id.zpw_payment_fail_rl_support);

        RelativeLayout.LayoutParams pUpdateInfo = (RelativeLayout.LayoutParams) rlUpdateInfo.getLayoutParams();
        RelativeLayout.LayoutParams pSupport = (RelativeLayout.LayoutParams) rlSupport.getLayoutParams();

        if (Arrays.equals(ESuggestActionType.UPDATE_INFO_DISPLAY.getValue(), suggestActions)) {
            setVisible(R.id.zpw_payment_fail_rl_support, false);
            setVisible(R.id.zpw_payment_fail_rl_update_info, true);
        } else if (Arrays.equals(ESuggestActionType.SUPPORT_DISPLAY.getValue(), suggestActions)) {
            setVisible(R.id.zpw_payment_fail_rl_support, true);
            setVisible(R.id.zpw_payment_fail_rl_update_info, false);
        } else if (Arrays.equals(ESuggestActionType.UPDATE_INFO_ABOVE.getValue(), suggestActions)) {
            setVisible(R.id.zpw_payment_fail_rl_update_info, true);
            setVisible(R.id.zpw_payment_fail_rl_support, true);
            pSupport.addRule(RelativeLayout.BELOW, rlUpdateInfo.getId());
            rlSupport.setLayoutParams(pSupport);
        } else if (Arrays.equals(ESuggestActionType.SUPPORT_ABOVE.getValue(), suggestActions)) {
            setVisible(R.id.zpw_payment_fail_rl_support, true);
            setVisible(R.id.zpw_payment_fail_rl_update_info, true);
            pUpdateInfo.addRule(RelativeLayout.BELOW, rlSupport.getId());
            rlUpdateInfo.setLayoutParams(pUpdateInfo);
        }
    }

    public void showMessageSnackBar(View pRootView, String pMessage, String pActionMessage, int pDuration, onCloseSnackBar pOnCloseListener) {
        PaymentSnackBar.getInstance().dismiss();
        try {
            PaymentSnackBar.getInstance().setRootView(pRootView)
                    .setBgColor(GlobalData.getAppContext().getResources().getColor(R.color.yellow_bg_popup_error))
                    .setMessage(pMessage)
                    .setActionMessage(pActionMessage)
                    .setDuration(pDuration)
                    .setOnCloseListener(pOnCloseListener)
                    .show();

        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    public void showDialogWarningLinkCardAndResetCardNumber() {
        showInfoDialog(GlobalData.getStringResource(RS.string.zpw_alert_linkcard_not_support), () -> mPresenter.resetCardNumberAndShowKeyBoard());
    }
}
