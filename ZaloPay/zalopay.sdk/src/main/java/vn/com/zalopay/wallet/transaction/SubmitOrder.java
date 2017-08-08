package vn.com.zalopay.wallet.transaction;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.api.AbstractRequest;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.constants.ConstantParams;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

/**
 * Created by chucvv on 6/16/17.
 */

public class SubmitOrder extends AbstractRequest<StatusResponse> {
    private long mAppId;
    private int mChannelId;
    private UserInfo mUserInfo;
    private AbstractOrder mOrder;
    private VoucherInfo mVoucher;
    private
    @TransactionType
    int mTranstype;
    private PaymentLocation mLocation;
    private String mChargeInfo;
    private String mHashPassword;

    public SubmitOrder(ITransService pTransService, long pAppId, UserInfo pUserInfo, PaymentLocation pLocation, @TransactionType int pTranstype) {
        super(pTransService);
        this.mTransService = pTransService;
        this.mAppId = pAppId;
        this.mUserInfo = pUserInfo;
        this.mLocation = pLocation;
        this.mTranstype = pTranstype;
    }

    public SubmitOrder voucher(VoucherInfo pVoucher) {
        this.mVoucher = pVoucher;
        return this;
    }

    public SubmitOrder order(AbstractOrder pOrder) {
        this.mOrder = pOrder;
        return this;
    }

    public SubmitOrder chargeInfo(String pChargeInfo) {
        this.mChargeInfo = pChargeInfo;
        return this;
    }

    public SubmitOrder channelId(int pChannelId) {
        this.mChannelId = pChannelId;
        return this;
    }

    public SubmitOrder password(String pHashPassword) {
        this.mHashPassword = pHashPassword;
        return this;
    }

    @Override
    public Map<String, String> buildParams() {
        Map<String, String> paramsApi = getMapTable();
        DataParameter.prepareSubmitTransactionParams(mChannelId, mAppId, mChargeInfo, mHashPassword,
                mOrder, mUserInfo, mLocation, mTranstype, mVoucher, paramsApi);
        return paramsApi;
    }

    private Observable<StatusResponse> submitTrans(Map<String, String> pParams) {
        return mTransService.submitOrder(
                pParams.get(ConstantParams.APP_ID), pParams.get(ConstantParams.ZALO_ID), pParams.get(ConstantParams.APP_TRANS_ID), pParams.get(ConstantParams.APP_USER),
                pParams.get(ConstantParams.APP_TIME), pParams.get(ConstantParams.ITEM), pParams.get(ConstantParams.DESCRIPTION), pParams.get(ConstantParams.EMBED_DATA),
                pParams.get(ConstantParams.MAC), pParams.get(ConstantParams.PLATFORM), pParams.get(ConstantParams.PLATFORM_CODE), pParams.get(ConstantParams.AMOUNT),
                pParams.get(ConstantParams.DEVICE_ID), pParams.get(ConstantParams.DEVICE_MODEL), pParams.get(ConstantParams.APP_VERSION), pParams.get(ConstantParams.SDK_VERSION),
                pParams.get(ConstantParams.OS_VERSION), pParams.get(ConstantParams.CONN_TYPE), pParams.get(ConstantParams.MNO), pParams.get(ConstantParams.PMC_ID), pParams.get(ConstantParams.CHARGE_INFO),
                pParams.get(ConstantParams.PIN), pParams.get(ConstantParams.TRANS_TYPE), pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.USER_ID), pParams.get(ConstantParams.LATTITUDE), pParams.get(ConstantParams.LONGITUDE),
                pParams.get(ConstantParams.ORDER_SOURCE), pParams.get(ConstantParams.VOUCHERINFO));

    }

    @Override
    public Observable<StatusResponse> getObserver() {
        return submitTrans(buildParams())
                .compose(applyState);
    }

    @Override
    protected void doOnNext(StatusResponse statusResponse) {
        super.doOnNext(statusResponse);
        ZPAnalyticsTrackerWrapper.trackApiCall(ZPEvents.CONNECTOR_V001_TPE_SUBMITTRANS, startTime, statusResponse);
    }

    @Override
    protected void doOnError(Throwable throwable) {
        super.doOnError(throwable);
        ZPAnalyticsTrackerWrapper.trackApiError(ZPEvents.CONNECTOR_V001_TPE_SUBMITTRANS, startTime, throwable);
    }
}
