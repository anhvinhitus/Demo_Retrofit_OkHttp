package vn.com.zalopay.wallet.datasource.interfaces;

import java.util.HashMap;

import retrofit2.Call;
import vn.com.zalopay.wallet.datasource.IData;

public interface ITask {
    Call doTask(IData pIData, HashMap<String, String> pParams) throws Exception;

    int getTaskEventId();
}
