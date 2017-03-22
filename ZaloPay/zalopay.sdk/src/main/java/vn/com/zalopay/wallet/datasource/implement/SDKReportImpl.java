package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Call;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;

public class SDKReportImpl implements ITask {
    @Override
    public Call doTask(IData pIData, HashMap<String, String> pParams) throws Exception {
        return pIData.sdkReport(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.TRANSID),
                pParams.get(ConstantParams.BANK_CODE),
                pParams.get(ConstantParams.EXINFO),
                pParams.get(ConstantParams.EXCEPTION));
    }

    @Override
    public int getTaskEventId() {
        return ZPEvents.CONNECTOR_V001_TPE_SDKERRORREPORT;
    }
}
