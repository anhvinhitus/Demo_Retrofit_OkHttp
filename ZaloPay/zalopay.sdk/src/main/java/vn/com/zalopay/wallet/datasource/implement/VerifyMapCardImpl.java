package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Call;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;

public class VerifyMapCardImpl implements ITask {
    @Override
    public Call doTask(IData pIData, HashMap<String, String> pParams) throws Exception {
        return pIData.verfiyCardMap(pParams.get(ConstantParams.USER_ID)
                , pParams.get(ConstantParams.ACCESS_TOKEN)
                , pParams.get(ConstantParams.ZALO_ID)
                , pParams.get(ConstantParams.DEVICE_ID)
                , pParams.get(ConstantParams.PLATFORM)
                , pParams.get(ConstantParams.SDK_VERSION)
                , pParams.get(ConstantParams.OS_VERSION)
                , pParams.get(ConstantParams.CONN_TYPE)
                , pParams.get(ConstantParams.MNO)
                , pParams.get(ConstantParams.DEVICE_MODEL)
                , pParams.get(ConstantParams.CARDINFO)
                , pParams.get(ConstantParams.APP_VERSION));
    }

    @Override
    public int getTaskEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_VERIFYCARDFORMAPPING;
    }
}
