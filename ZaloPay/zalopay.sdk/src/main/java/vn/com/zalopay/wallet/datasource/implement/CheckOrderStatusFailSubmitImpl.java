package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Call;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;

public class CheckOrderStatusFailSubmitImpl implements ITask {
    @Override
    public Call doTask(IData pIData, HashMap<String, String> pParams) throws Exception {
        return pIData.checkOrderStatusFailSubmit(pParams);
    }

    @Override
    public int getTaskEventId() {
        return ZPEvents.API_V001_TPE_GETSTATUSBYAPPTRANSIDFORCLIENT;
    }
}