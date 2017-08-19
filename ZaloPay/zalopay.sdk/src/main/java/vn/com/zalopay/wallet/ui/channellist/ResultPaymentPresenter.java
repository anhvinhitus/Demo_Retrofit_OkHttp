package vn.com.zalopay.wallet.ui.channellist;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import timber.log.Timber;
import vn.com.zalopay.feedback.FeedbackCollector;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.ISdkErrorContext;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.configure.RS;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.entity.Feedback;
import vn.com.zalopay.wallet.entity.UserInfo;
import vn.com.zalopay.wallet.entity.bank.BaseMap;
import vn.com.zalopay.wallet.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.entity.response.StatusResponse;
import vn.com.zalopay.wallet.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.AbstractPresenter;


/*
 * Created by chucvv on 6/12/17.
 */

public class ResultPaymentPresenter extends AbstractPresenter<ResultPaymentFragment> implements ISdkErrorContext {
    protected PaymentInfoHelper mPaymentInfoHelper;
    Context mContext;
    StatusResponse mStatusResponse;

    public ResultPaymentPresenter() {
        Timber.d("call constructor ResultPaymentPresenter");
        mPaymentInfoHelper = GlobalData.getPaymentInfoHelper();
        mContext = GlobalData.getAppContext();
    }

    public boolean onBackPressed() {
        if (mView.visualSupportView()) {
            mView.onCloseSupportView();
            return true;
        }
        callback();
        mView.terminate();
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Timber.d("onDetach");
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    protected void callback() {
        Timber.d("callback");
        if (GlobalData.getPaymentListener() != null) {
            GlobalData.getPaymentListener().onComplete();
        }
    }

    void showFeedbackDialog() throws Exception {
        if (!validPaymentInfo()) {
            getViewOrThrow().showError(mContext.getResources().getString(R.string.sdk_error_paymentinfo_empty));
            return;
        }
        FeedBackCollector feedBackCollector = FeedBackCollector.shared();
        String transTitle = mPaymentInfoHelper.getTitleByTrans(mContext);
        int errorCode = mStatusResponse != null ? mStatusResponse.returncode : Constants.NULL_ERRORCODE;
        String transFailReason = mStatusResponse != null ? mStatusResponse.returnmessage : "";
        String transId = mStatusResponse != null ? mStatusResponse.zptransid : "";
        Feedback feedBack = Feedback.collectFeedBack(getViewOrThrow().getActivity(), transTitle, transFailReason, errorCode, transId);
        if (feedBack != null) {
            FeedbackCollector collector = feedBackCollector.getFeedbackCollector();
            collector.setScreenShot(feedBack.imgByteArray);
            collector.setTransaction(feedBack.category, feedBack.transID, feedBack.errorCode, feedBack.description);
        }
        feedBackCollector.showDialog(getViewOrThrow().getActivity());
    }

    void onPaymentButtonClick() {
        callback();
        mView.terminate();
    }

    private String getPaymentCardKey() {
        return mPaymentInfoHelper != null && mPaymentInfoHelper.getMapBank() != null ?
                mPaymentInfoHelper.getMapBank().getKey() : "";
    }

    boolean validPaymentInfo() {
        if (mPaymentInfoHelper == null) {
            mPaymentInfoHelper = GlobalData.getPaymentInfoHelper();
        }
        return mPaymentInfoHelper != null && mStatusResponse != null;
    }

    void showResultPayment(StatusResponse pResponse) {
        try {
            mStatusResponse = pResponse;
            if (!validPaymentInfo()) {
                getViewOrThrow().showError(mContext.getResources().getString(R.string.sdk_error_paymentinfo_empty));
                return;
            }
            GlobalData.extraJobOnPaymentCompleted(mStatusResponse, getBankCode());
            boolean success = TransactionHelper.isTransactionSuccess(pResponse);
            if (success) {
                doOnSuccessPayment();
            } else {
                doOnFailurePayment();
            }
            getViewOrThrow().dismissShowingView();
        } catch (Exception e) {
            Timber.w(e, "Exception show result payment");
        }
    }

    private void doOnSuccessPayment() throws Exception {
        renderSuccessPaymentView();
        //save payment card for show on channel list later
        String userId = mPaymentInfoHelper != null ? mPaymentInfoHelper.getUserId() : null;
        String paymentCard = getPaymentCardKey();
        if (!TextUtils.isEmpty(userId)) {
            SDKApplication
                    .getApplicationComponent()
                    .bankListInteractor()
                    .setPaymentBank(userId, paymentCard);
            Timber.d("update recently payment bank for auto select %s", GsonUtils.toJsonString(paymentCard));
        }
    }

    private void doOnFailurePayment() throws Exception {
        renderFailurePaymentView();
        SDKApplication
                .sdkErrorReporter()
                .sdkReportErrorOnTransactionFail(this,
                        GsonUtils.toJsonString(mStatusResponse));
    }

    private void renderSuccessPaymentView() throws Exception {
        try {
            if (mView == null) {
                Timber.w("mView is null");
                return;
            }
            if (mPaymentInfoHelper == null) {
                Timber.w("payment info is null");
                return;
            }
            if (mStatusResponse == null) {
                Timber.w("status response is null");
                return;
            }
            String pageName = Constants.PAGE_SUCCESS;
            mView.renderByResource(pageName);
            long appId = mPaymentInfoHelper.getAppId();
            AppInfo appInfo = TransactionHelper.getAppInfoCache(appId);
            int transtype = mPaymentInfoHelper.getTranstype();
            String appName = TransactionHelper.getAppNameByTranstype(mContext, transtype);
            if (TextUtils.isEmpty(appName)) {
                appName = appInfo != null ? appInfo.appname : null;
            }
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            boolean isTransfer = mPaymentInfoHelper.isMoneyTranferTrans();
            UserInfo receiverInfo = mPaymentInfoHelper.getMoneyTransferReceiverInfo();
            String title = mPaymentInfoHelper.getSuccessTitleByTrans(mContext);
            boolean isLink = mPaymentInfoHelper.isLinkTrans();
            String transId = mStatusResponse.zptransid;
            AbstractOrder order = mPaymentInfoHelper.getOrder();
            if (mPaymentInfoHelper.isMobileCardApp()) {
                order.description = GlobalData.getStringResource(RS.string.app_mobilecard_desc);
            }
            mView.renderSuccess(isLink, transId, userInfo, order, appName, null, isLink, isTransfer, receiverInfo, title);
        } catch (Exception e) {
            Timber.w(e, "Exception render success info");
        }
    }

    private void renderFailurePaymentView() throws Exception {
        try {
            if (mView == null) {
                Timber.w("mView is null");
                return;
            }
            if (mPaymentInfoHelper == null) {
                Timber.w("payment info is null");
                return;
            }
            if (mStatusResponse == null) {
                Timber.w("status response is null");
                return;
            }
            String message = mStatusResponse.returnmessage;
            if (TextUtils.isEmpty(message)) {
                message = mContext.getResources().getString(R.string.sdk_payment_generic_error_networking_mess);
            }
            String pageName = Constants.PAGE_FAIL;
            if (TransactionHelper.isTransactionProcessing(mContext, message, mPaymentInfoHelper.getTranstype())) {
                pageName = Constants.PAGE_FAIL_PROCESSING;
            } else if (TransactionHelper.isTransNetworkError(mContext, message)) {
                pageName = Constants.PAGE_FAIL_NETWORKING;
                mPaymentInfoHelper.updateResultNetworkingError(mContext, message); //update payment status to no internet to app know
            }
            int status = mPaymentInfoHelper.getStatus();
            if (status != PaymentStatus.TOKEN_EXPIRE && status != PaymentStatus.USER_LOCK) {
                mPaymentInfoHelper.setResult(pageName.equals(Constants.PAGE_FAIL_PROCESSING) ? PaymentStatus.NON_STATE : PaymentStatus.FAILURE);
            }
            mView.renderByResource(pageName);

            String appName = TransactionHelper.getAppNameByTranstype(mContext, mPaymentInfoHelper.getTranstype());
            if (TextUtils.isEmpty(appName)) {
                AppInfo appInfo = TransactionHelper.getAppInfoCache(mPaymentInfoHelper.getAppId());
                appName = appInfo != null ? appInfo.appname : null;
            }
            String title = mPaymentInfoHelper.getFailTitleByTrans(mContext);
            boolean isLink = mPaymentInfoHelper.isLinkTrans();
            String transId = mStatusResponse.zptransid;
            mView.renderFail(isLink, message, transId, mPaymentInfoHelper.getOrder(), appName, mStatusResponse, true, title);
        } catch (Exception e) {
            Timber.w(e, "Exception render fail info");
        }
    }

    @Override
    public boolean hasCardGuiProcessor() {
        return false;
    }

    @Override
    public String getBankCode() {
        if (mPaymentInfoHelper == null) {
            return "";
        }
        BaseMap map = mPaymentInfoHelper.getMapBank();
        return map != null ? map.bankcode : "";
    }

    @Override
    public String getTransactionId() {
        return mStatusResponse != null ? mStatusResponse.zptransid : "";
    }

    @Override
    public UserInfo getUserInfo() {
        if (mPaymentInfoHelper == null) {
            return null;
        }
        return mPaymentInfoHelper.getUserInfo();
    }
}
