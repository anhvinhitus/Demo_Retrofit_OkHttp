package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.business.entity.base.StatusResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class SubmitOrderImpl implements IRequest<StatusResponse> {
    @Override
    public Observable<Response<StatusResponse>> getRequest(IData pIData, HashMap<String, String> pParams) {
        return pIData.submitOrder(
                pParams.get(ConstantParams.APP_ID), pParams.get(ConstantParams.ZALO_ID), pParams.get(ConstantParams.APP_TRANS_ID), pParams.get(ConstantParams.APP_USER),
                pParams.get(ConstantParams.APP_TIME), pParams.get(ConstantParams.ITEM), pParams.get(ConstantParams.DESCRIPTION), pParams.get(ConstantParams.EMBED_DATA),
                pParams.get(ConstantParams.MAC), pParams.get(ConstantParams.PLATFORM), pParams.get(ConstantParams.PLATFORM_CODE), pParams.get(ConstantParams.AMOUNT),
                pParams.get(ConstantParams.DEVICE_ID), pParams.get(ConstantParams.DEVICE_MODEL), pParams.get(ConstantParams.APP_VERSION), pParams.get(ConstantParams.SDK_VERSION),
                pParams.get(ConstantParams.OS_VERSION), pParams.get(ConstantParams.CONN_TYPE), pParams.get(ConstantParams.MNO), pParams.get(ConstantParams.PMC_ID), pParams.get(ConstantParams.CHARGE_INFO),
                pParams.get(ConstantParams.PIN), pParams.get(ConstantParams.TRANS_TYPE), pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.USER_ID), pParams.get(ConstantParams.LATTITUDE), pParams.get(ConstantParams.LONGITUDE));
    }

    @Override
    public int getRequestEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_SUBMITTRANS;
    }
}
