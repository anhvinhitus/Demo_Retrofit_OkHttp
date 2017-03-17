package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import java.util.List;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.business.objectmanager.SingletonLifeCircleManager;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.RemoveMapCardImpl;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class RemoveMapCard extends BaseRequest<BaseResponse> {
    private ZPWRemoveMapCardParams mMapCardParams;
    private ZPWRemoveMapCardListener mListener;

    public RemoveMapCard(ZPWRemoveMapCardParams pMapCardParams, ZPWRemoveMapCardListener pListener) {
        super();
        mMapCardParams = pMapCardParams;
        mListener = pListener;
    }

    private void reloadMapCardList() {
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mMapCardParams.userID;
        userInfo.accessToken = mMapCardParams.accessToken;
        GlobalData.setUserInfo(userInfo);

        MapCardHelper.loadMapCardList(true, new IReloadMapInfoListener<DMappedCard>() {
            @Override
            public void onComplete(List<DMappedCard> pMapCardList) {
                callbackSuccessToMerchant();
                Log.d(this, "===onComplete===" + GsonUtils.toJsonString(pMapCardList));
            }

            @Override
            public void onError(String pErrorMess) {
                callbackSuccessToMerchant();
                Log.d(this, "===onError=" + pErrorMess);
            }
        });
    }

    private void callbackSuccessToMerchant() {
        if (mListener != null) {
            mListener.onSuccess(mMapCardParams.mapCard);
        }
        SingletonLifeCircleManager.disposeAll();
    }

    private void onPostResult() {
        if (getResponse() != null && mListener != null) {
            if (getResponse().returncode > 0) {
                try {
                    SharedPreferencesManager.getInstance().removeMappedCard(mMapCardParams.userID + Constants.COMMA + mMapCardParams.mapCard.getCardKey());
                    reloadMapCardList();
                } catch (Exception e) {
                    Log.e(this, e);
                    callbackSuccessToMerchant();
                }
            } else {
                mListener.onError(getResponse());
            }
        } else if (mListener != null) {
            createReponse(-1, GlobalData.getStringResource(RS.string.zpw_string_alert_remove_mapcard_error_networking));
            mListener.onError(getResponse());
        } else {
            Log.e(this, "===mListener = NULL===");
        }
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        onPostResult();
    }

    @Override
    protected void onRequestFail(String pMessage) {
        if (getResponse() == null) {
            createReponse(-1, GlobalData.getStringResource(RS.string.zpw_string_alert_remove_mapcard_error_networking));
        }

        if (!TextUtils.isEmpty(pMessage)) {
            mResponse.returnmessage = pMessage;
        }

        onPostResult();
    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        mResponse = new BaseResponse();
        mResponse.returnmessage = pMessage;
        mResponse.returncode = pCode;
    }

    @Override
    protected void onRequestInProcess() {
        Log.d(this, "==removing map card===" + GsonUtils.toJsonString(mMapCardParams));
    }

    @Override
    protected void doRequest() {
        try {
            shareDataRepository().pushData(new RemoveMapCardImpl(), getDataParams());
            SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareRemoveCard(getDataParams(), mMapCardParams);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }

        return true;
    }
}
