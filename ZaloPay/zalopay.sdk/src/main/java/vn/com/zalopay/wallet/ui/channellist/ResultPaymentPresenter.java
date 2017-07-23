package vn.com.zalopay.wallet.ui.channellist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.zalopay.feedback.FeedbackCollector;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.api.ISdkErrorContext;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.feedback.Feedback;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.AppInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.PaymentStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.feedback.FeedBackCollector;
import vn.com.zalopay.wallet.helper.TransactionHelper;
import vn.com.zalopay.wallet.paymentinfo.PaymentInfoHelper;
import vn.com.zalopay.wallet.ui.AbstractPresenter;


/*
 * Created by chucvv on 6/12/17.
 */

public class ResultPaymentPresenter extends AbstractPresenter<ResultPaymentFragment> implements ISdkErrorContext {
    protected PaymentInfoHelper mPaymentInfoHelper;
    @Inject
    Context mContext;
    StatusResponse mResponse;

    public ResultPaymentPresenter() {
        Timber.d("call constructor ResultPaymentPresenter");
        mPaymentInfoHelper = GlobalData.paymentInfoHelper;
        SDKApplication.getApplicationComponent().inject(this);
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

    public void showFeedbackDialog() throws Exception {
        FeedBackCollector feedBackCollector = FeedBackCollector.shared();
        String transTitle = mPaymentInfoHelper.getTitleByTrans(mContext);
        int errorCode = mResponse != null ? mResponse.returncode : Constants.NULL_ERRORCODE;
        String transFailReason = mResponse != null ? mResponse.returnmessage : "";
        String transId = mResponse != null ? mResponse.zptransid : "";
        Feedback feedBack = Feedback.collectFeedBack(getViewOrThrow().getActivity(), transTitle, transFailReason, errorCode, transId);
        if (feedBack != null) {
            FeedbackCollector collector = feedBackCollector.getFeedbackCollector();
            collector.setScreenShot(feedBack.imgByteArray);
            collector.setTransaction(feedBack.category, feedBack.transID, feedBack.errorCode, feedBack.description);
        }
        feedBackCollector.showDialog(getViewOrThrow().getActivity());
    }


    private Feedback collectFeedBack() {
        Feedback feedBack = null;
        try {
            Bitmap mBitmap = SdkUtils.CaptureScreenshot(getViewOrThrow().getActivity());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (mBitmap != null) {
                mBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            }
            byte[] byteArray = stream.toByteArray();
            String transactionTitle = mPaymentInfoHelper.getTitleByTrans(mContext);
            int errorcode = mResponse.returncode;
            feedBack = new Feedback(byteArray, mResponse.returnmessage, transactionTitle, mResponse.zptransid, errorcode);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return feedBack;
    }

    void onPaymentButtonClick() {
        callback();
        mView.terminate();
    }

    void showResultPayment(StatusResponse pResponse) {
        try {
            mResponse = pResponse;
            GlobalData.extraJobOnPaymentCompleted(mResponse, getDetectedBankCode());
            boolean success = TransactionHelper.isTransactionSuccess(pResponse);
            if (success) {
                showSuccessPayment(pResponse);
            } else {
                showFailurePayment(pResponse);
                try {
                    SDKApplication
                            .sdkErrorReporter()
                            .sdkReportErrorOnTransactionFail(this, GsonUtils.toJsonString(mResponse));
                } catch (Exception e) {
                    Timber.w(e, "Exception send error log");
                }
            }
        } catch (Exception e) {
            Timber.w(e, "Exception show result payment");
        }
    }

    private void showSuccessPayment(StatusResponse pResponse) throws Exception {
        try {
            String pageName = Constants.PAGE_SUCCESS;
            mView.renderByResource(pageName);
            AppInfo appInfo = TransactionHelper.getAppInfoCache(mPaymentInfoHelper.getAppId());
            String appName = TransactionHelper.getAppNameByTranstype(mContext, mPaymentInfoHelper.getTranstype());
            if (TextUtils.isEmpty(appName)) {
                appName = appInfo != null ? appInfo.appname : null;
            }
            UserInfo userInfo = mPaymentInfoHelper.getUserInfo();
            boolean isTransfer = mPaymentInfoHelper.isMoneyTranferTrans();
            UserInfo receiverInfo = mPaymentInfoHelper.getMoneyTransferReceiverInfo();
            String title = mPaymentInfoHelper.getSuccessTitleByTrans(mContext);
            boolean isLink = mPaymentInfoHelper.isLinkTrans();
            String transId = pResponse.zptransid;
            mView.renderSuccess(isLink, transId, userInfo, mPaymentInfoHelper.getOrder(), appName, null, isLink, isTransfer, receiverInfo, title);
        } catch (Exception e) {
            Timber.w(e, "Exception render success info");
        }
    }

    private void showFailurePayment(StatusResponse pResponse) throws Exception {
        try {
            String message = pResponse.returnmessage;
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
            String transId = pResponse.zptransid;
            mView.renderFail(isLink, message, transId, mPaymentInfoHelper.getOrder(), appName, pResponse, true, title);
        } catch (Exception e) {
            Timber.w(e, "Exception render fail info");
        }
    }

    @Override
    public boolean hasCardGuiProcessor() {
        return false;
    }

    @Override
    public String getDetectedBankCode() {
        if (mPaymentInfoHelper == null) {
            return "";
        }
        BaseMap map = mPaymentInfoHelper.getMapBank();
        return map != null ? map.bankcode : "";
    }

    @Override
    public String getTransactionId() {
        return mResponse != null ? mResponse.zptransid : "";
    }

    @Override
    public UserInfo getUserInfo() {
        if (mPaymentInfoHelper == null) {
            return null;
        }
        return mPaymentInfoHelper.getUserInfo();
    }
}
