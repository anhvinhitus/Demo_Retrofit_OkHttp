package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Call;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;

public class GetBankAccountListImpl implements ITask {
    @Override
    public Call doTask(IData pIData, HashMap<String, String> pParams) {
        return pIData.getBankAccountList(pParams);
    }

    @Override
    public int getTaskEventId() {
        return ZPEvents.API_UM_LISTBANKACCOUNTFORCLIENT;
    }
}
