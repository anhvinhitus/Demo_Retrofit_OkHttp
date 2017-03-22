package vn.com.zalopay.wallet.datasource.implement;


import java.util.HashMap;

import retrofit2.Call;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;

public class AuthenPayerImpl implements ITask {
    @Override
    public Call doTask(IData pIData, HashMap<String, String> pParams) throws Exception {
        return pIData.atmAuthen(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.ZP_TRANSID),
                pParams.get(ConstantParams.AUTHEN_TYPE),
                pParams.get(ConstantParams.AUTHEN_VALUE),
                pParams.get(ConstantParams.APP_VERSION));
    }

    @Override
    public int getTaskEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_ATMAUTHENPAYER;
    }
}
