package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetMapCardInfoListImpl;
import vn.com.zalopay.wallet.listener.IGetMapCardInfo;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.Log;

public class GetMapCardInfoList extends BaseRequest<CardInfoListResponse> {
    private AdapterBase mAdapter;
    private IGetMapCardInfo mGetCardInfoCallBack;

    public GetMapCardInfoList(AdapterBase pAdapter) {
        super();
        this.mAdapter = pAdapter;
        this.mGetCardInfoCallBack = null;
    }

    public GetMapCardInfoList(IGetMapCardInfo pGetCardInfoCallBack) {
        super();
        this.mAdapter = null;
        this.mGetCardInfoCallBack = pGetCardInfoCallBack;
    }

    private void onPostResult() {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_GET_CARDINFO_LIST_COMPLETE, getResponse());
        }
        if (mGetCardInfoCallBack != null) {
            mGetCardInfoCallBack.onGetCardInfoComplete(getResponse());
        }
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        onPostResult();
    }

    @Override
    protected void onRequestFail(String pMessage) {
        if (getResponse() == null) {
            createReponse(-1, pMessage);
        }
        getResponse().returncode = -1;
        getResponse().returnmessage = TextUtils.isEmpty(pMessage) ? pMessage : GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error);

        onPostResult();
    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        mResponse = new CardInfoListResponse();
    }

    @Override
    protected void onRequestInProcess() {

    }

    @Override
    protected void doRequest() {
        try {
            //Lost connection
            if (!ConnectionUtil.isOnline(GlobalData.getAppContext()) && mAdapter != null) {
                onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_save_card_error));
                return;
            }

           newDataRepository().getData(new GetMapCardInfoListImpl(), getDataParams());
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareGetCardInfoListParams(getDataParams());
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }
        return true;
    }
}
