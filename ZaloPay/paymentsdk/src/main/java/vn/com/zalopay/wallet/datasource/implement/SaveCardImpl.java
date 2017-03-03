package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Call;
import vn.com.zalopay.wallet.business.data.ConstantParams;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;

public class SaveCardImpl implements ITask {
    @Override
    public Call doTask(IData pIData, HashMap<String, String> pParams) throws Exception {
        return pIData.saveCard(pParams.get(ConstantParams.USER_ID),
                pParams.get(ConstantParams.ZP_TRANSID),
                pParams.get(ConstantParams.ACCESS_TOKEN),
                pParams.get(ConstantParams.BANK_CODE),
                pParams.get(ConstantParams.FIRST6_CARDNO),
                pParams.get(ConstantParams.LAST4_CARDNO),
                pParams.get(ConstantParams.APP_VERSION));
    }

    @Override
    public int getTaskEventId() {
        return 0;
    }
}
