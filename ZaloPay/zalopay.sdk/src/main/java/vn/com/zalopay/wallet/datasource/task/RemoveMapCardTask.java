package vn.com.zalopay.wallet.datasource.task;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.RemoveMapCardImpl;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.listener.ZPWRemoveMapCardListener;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.data.Log;

public class RemoveMapCardTask extends BaseTask<BaseResponse> {
    private ZPWRemoveMapCardParams mMapCardParams;
    private ZPWRemoveMapCardListener mListener;

    public RemoveMapCardTask(ZPWRemoveMapCardParams pMapCardParams, ZPWRemoveMapCardListener pListener) {
        super();
        mMapCardParams = pMapCardParams;
        mListener = pListener;
    }

    private void reloadMapCardList() {
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mMapCardParams.userID;
        userInfo.accessToken = mMapCardParams.accessToken;

        MapCardHelper.loadMapCardList(true, userInfo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<BaseResponse>() {
                    @Override
                    public void onSuccess(BaseResponse response) {
                        callbackSuccessToMerchant();
                    }

                    @Override
                    public void onError(Throwable error) {
                        onRequestFail(error);
                        Log.d(this, error);
                    }
                });
    }

    private void callbackSuccessToMerchant() {
        if (mListener != null) {
            mListener.onSuccess(mMapCardParams.mapCard);
        }
    }

    @Override
    public void onDoTaskOnResponse(BaseResponse pResponse) {
        Log.d(this, "onDoTaskOnResponse do nothing");
    }

    @Override
    public void onRequestSuccess(BaseResponse pResponse) {
        if (!(pResponse instanceof BaseResponse)) {
            onRequestFail(null);
        } else if (pResponse.returncode >= 0) {
            try {
                SharedPreferencesManager.getInstance().removeMappedCard(mMapCardParams.userID + Constants.COMMA + mMapCardParams.mapCard.getCardKey());
                reloadMapCardList();//reload map card list to refresh checksum and map list on cache
            } catch (Exception e) {
                Log.e(this, e);
                callbackSuccessToMerchant();
            }
        } else if (mListener != null) {
            mListener.onError(pResponse);
        } else {
            Log.e(this, "mListener = NULL");
        }
        Log.d(this, "onRequestSuccess");
    }

    @Override
    public void onRequestFail(Throwable e) {
        if (ConnectionUtil.isOnline(GlobalData.getAppContext())) // has error network but device is online::need to reload map list
        {
            reloadMapCardList();
        } else if (mListener != null) {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.returncode = -1;
            baseResponse.returnmessage = getDefaulErrorNetwork();
            mListener.onError(baseResponse);
        } else {
            Log.e(this, "mListener = NULL");
        }
        Log.d(this, e);
    }

    @Override
    public void onRequestInProcess() {
        Log.d(this, "onRequestInProcess " + GsonUtils.toJsonString(mMapCardParams));
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zpw_alert_network_error_removemapcard);
    }

    @Override
    protected void doRequest() {
        shareDataRepository().setTask(this).postData(new RemoveMapCardImpl(), getDataParams());
        try {
            SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareRemoveCard(getDataParams(), mMapCardParams);
            return true;
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
            return false;
        }
    }
}
