package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.LoadBankListImpl;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class BankListTask extends BaseTask<BankConfigResponse> {
    private static final String TAG = BankListTask.class.getCanonicalName();
    private ILoadBankListListener mILoadBankListListener;

    public BankListTask(ILoadBankListListener pILoadBankListListener) {
        super();
        mILoadBankListListener = pILoadBankListListener;
    }

    @Override
    protected void doRequest() {
        try {
            shareDataRepository().setTask(this).loadData(new LoadBankListImpl(), getDataParams());
        } catch (Exception e) {
            onRequestFail(null);
            Log.e(this, e);
        }
    }

    protected boolean isChangedCheckSumBankList(String pNewCheckSum) {
        String checkSumOnCache = null;
        try {
            checkSumOnCache = SharedPreferencesManager.getInstance().getCheckSumBankList();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return TextUtils.isEmpty(checkSumOnCache) || (!TextUtils.isEmpty(pNewCheckSum) && !checkSumOnCache.equalsIgnoreCase(pNewCheckSum));
    }

    protected void saveBankListToCache(BankConfigResponse pResponse) throws Exception {
        //save bank list to cache
        long time_to_live = System.currentTimeMillis() + pResponse.expiredtime;
        SharedPreferencesManager.getInstance().setExpiredBankList(time_to_live);
        SharedPreferencesManager.getInstance().setCheckSumBankList(pResponse.checksum);
        for (BankConfig bankConfig : pResponse.banklist) {
            SharedPreferencesManager.getInstance().setBankConfig(bankConfig.code, GsonUtils.toJsonString(bankConfig));
        }
        String hashMapBank = GsonUtils.toJsonString(pResponse.bankcardprefixmap);
        SharedPreferencesManager.getInstance().setBankConfigMap(hashMapBank);
        Log.d(this, "saved bank list to cache " + GsonUtils.toJsonString(pResponse));
    }

    @Override
    public void onDoTaskOnResponse(BankConfigResponse pResponse) {
        Log.d(this, "onSaveResponseToDisk");
        if(pResponse == null || pResponse.returncode != 1)
        {
            Log.d(this,"request not success...stopping saving response to cache");
            return;
        }
        if (isChangedCheckSumBankList(pResponse.checksum)) {
            try {
                saveBankListToCache(pResponse);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (pResponse.bankcardprefixmap != null) {
            BankLoader.mapBank = pResponse.bankcardprefixmap;
        } else {
            java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            try {
                HashMap<String, String> bankMap = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankMap(), type);
                BankLoader.mapBank = bankMap;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    @Override
    public void onRequestSuccess(BankConfigResponse pResponse) {
        if (!(pResponse instanceof BankConfigResponse)) {
            onRequestFail(null);
        } else if (pResponse.returncode < 0 && mILoadBankListListener != null) {
            mILoadBankListListener.onError(pResponse.getMessage());
        } else if (mILoadBankListListener != null) {
            mILoadBankListListener.onComplete();
        } else {
            Log.d(this, "mILoadBankListListener = NULL");
        }
        Log.d(this, "onRequestSuccess");
    }

    @Override
    public void onRequestFail(Throwable e) {
        if (mILoadBankListListener != null) {
            mILoadBankListListener.onError(getDefaulErrorNetwork());
        }
        Log.d(TAG, e);
    }

    @Override
    public void onRequestInProcess() {
        if (mILoadBankListListener != null) {
            mILoadBankListListener.onProcessing();
        }
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zpw_alert_network_error_loadbanklist);
    }

    @Override
    protected boolean doParams() {
        try {
            String pCheckSum = SharedPreferencesManager.getInstance().getCheckSumBankList();
            DataParameter.prepareGetBankList(mDataParams, pCheckSum);
            return true;
        } catch (Exception e) {
            onRequestFail(null);
            Log.e(this, e);
            return false;
        }
    }
}
