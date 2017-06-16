package vn.com.zalopay.wallet.transaction;

import android.os.Build;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.api.DataParameter;
import vn.com.zalopay.wallet.api.ITransService;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.PaymentLocation;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.paymentinfo.AbstractOrder;

/**
 * Created by chucvv on 6/16/17.
 */

public class TransactionProcessor {
    private ITransService mTransService;
    private long mAppId;
    private int mChannelId;
    private UserInfo mUserInfo;
    private AbstractOrder mOrder;
    private
    @TransactionType
    int mTranstype;
    private PaymentLocation mLocation;
    private String mChargeInfo;
    private String mHashPassword;

    public TransactionProcessor(ITransService pTransService, long pAppId, UserInfo pUserInfo, PaymentLocation pLocation, @TransactionType int pTranstype) {
        this.mTransService = pTransService;
        this.mAppId = pAppId;
        this.mUserInfo = pUserInfo;
        this.mLocation = pLocation;
        this.mTranstype = pTranstype;
    }

    public TransactionProcessor setOrder(AbstractOrder pOrder) {
        this.mOrder = pOrder;
        return this;
    }

    public TransactionProcessor setChargeInfo(String pChargeInfo) {
        this.mChargeInfo = pChargeInfo;
        return this;
    }

    public TransactionProcessor setChannelId(int pChannelId) {
        this.mChannelId = pChannelId;
        return this;
    }

    public TransactionProcessor setPassword(String pHashPassword) {
        this.mHashPassword = pHashPassword;
        return this;
    }

    private Map<String, String> buildParams() {
        Map<String, String> paramsApi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            paramsApi = new ArrayMap<>();
        } else {
            paramsApi = new HashMap<>();
        }
        DataParameter.prepareSubmitTransactionParams(mChannelId, mAppId, mChargeInfo, mHashPassword,
                mOrder, mUserInfo, mLocation, mTranstype, paramsApi);
        return paramsApi;
    }

    private Observable<StatusResponse> observerSubmitTrans(Map<String, String> pParams) {
        return mTransService.submitOrder(
                pParams.get(ConstantParams.APP_ID), pParams.get(ConstantParams.ZALO_ID), pParams.get(ConstantParams.APP_TRANS_ID), pParams.get(ConstantParams.APP_USER),
                pParams.get(ConstantParams.APP_TIME), pParams.get(ConstantParams.ITEM), pParams.get(ConstantParams.DESCRIPTION), pParams.get(ConstantParams.EMBED_DATA),
                pParams.get(ConstantParams.MAC), pParams.get(ConstantParams.PLATFORM), pParams.get(ConstantParams.PLATFORM_CODE), pParams.get(ConstantParams.AMOUNT),
                pParams.get(ConstantParams.DEVICE_ID), pParams.get(ConstantParams.DEVICE_MODEL), pParams.get(ConstantParams.APP_VERSION), pParams.get(ConstantParams.SDK_VERSION),
                pParams.get(ConstantParams.OS_VERSION), pParams.get(ConstantParams.CONN_TYPE), pParams.get(ConstantParams.MNO), pParams.get(ConstantParams.PMC_ID), pParams.get(ConstantParams.CHARGE_INFO),
                pParams.get(ConstantParams.PIN), pParams.get(ConstantParams.TRANS_TYPE), pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.USER_ID), pParams.get(ConstantParams.LATTITUDE), pParams.get(ConstantParams.LONGITUDE), pParams.get(ConstantParams.ORDER_SOURCE));

    }

    public Observable<StatusResponse> getObserver() {
        Map<String, String> pParams = buildParams();
        return observerSubmitTrans(pParams);
    }
}
