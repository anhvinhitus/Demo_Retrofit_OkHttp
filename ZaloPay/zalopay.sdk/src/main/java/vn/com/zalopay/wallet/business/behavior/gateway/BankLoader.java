package vn.com.zalopay.wallet.business.behavior.gateway;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.request.BaseRequest;
import vn.com.zalopay.wallet.datasource.request.GetBankList;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class BankLoader extends SingletonBase {
    private static final String TAG = BankCardCheck.class.getName();

    private static BankLoader _object;

    public BankConfig maintenanceBank;
    public BankFunction maintenanceBankFunction;

    public BankLoader() {
        super();

        maintenanceBank = null;
        maintenanceBankFunction = null;
    }

    public static synchronized BankLoader getInstance() {
        if (BankLoader._object == null)
            BankLoader._object = new BankLoader();
        return BankLoader._object;
    }

    public static boolean existedBankListOnMemory() {
        return (BankCardCheck.mBankMap != null && BankCardCheck.mBankMap.size() > 0);
    }

    /***
     * need to reload bank list if
     * 1. timeout
     * 2. checksum is null
     *
     * @return
     * @throws Exception
     */
    protected static boolean isNeedToLoadBankList() throws Exception {
        long expiredTime = SharedPreferencesManager.getInstance().getExpiredBankList();
        String checkSum = SharedPreferencesManager.getInstance().getCheckSumBankList();
        String cachedBankList = SharedPreferencesManager.getInstance().getBankMap();

        if (expiredTime < System.currentTimeMillis() || TextUtils.isEmpty(cachedBankList) || TextUtils.isEmpty(checkSum)) {
            return true;
        }

        return false;
    }

    public synchronized static void loadBankList(ILoadBankListListener pListener) {
        try {
            if (isNeedToLoadBankList()) {
                //there a loading bank list task is running
                if (GetBankList.isLoading()) {
                    Log.d("loadBankList", "===there're a task is running");
                    return;
                }

                BaseRequest getBankList = new GetBankList(pListener);
                getBankList.makeRequest();
                return;
            }
            //bank list is on memory now
            if (BankLoader.existedBankListOnMemory()) {
                Log.d("loadBankList", "===bank list is cached on memory===");

                if (pListener != null) {
                    pListener.onComplete();
                }
                return;
            }

            Log.d("loadBankList", "===reload banklist on Cache===");

            java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
            }.getType();
            HashMap<String, String> bankMap = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankMap(), type);
            BankCardCheck.mBankMap = bankMap;
            if (pListener != null) {
                pListener.onComplete();
            }

        } catch (Exception e) {
            if (pListener != null) {
                pListener.onError(null);
            }
            Log.e(TAG, e != null ? e.getMessage() : "error");
        }
    }

    public BankConfig getBankByBankCode(String pBankCode) {
        if (!TextUtils.isEmpty(pBankCode)) {
            try {
                BankConfig bankConfig = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankConfig(pBankCode), BankConfig.class);
                return bankConfig;
            } catch (Exception e) {
                Log.e(this, e);
            }
        }

        return null;
    }

    /***
     * is bank maintenance
     *
     * @param pBankCode
     * @return
     */
    public boolean isBankMaintenance(String pBankCode) {
        BankConfig bankConfig = getBankByBankCode(pBankCode);
        if (bankConfig != null && bankConfig.isBankMaintenence(GlobalData.getCurrentBankFunction())) {
            maintenanceBank = bankConfig;
            maintenanceBankFunction = bankConfig.getBankFunction(GlobalData.getCurrentBankFunction());
            return true;
        }
        return false;
    }

    /***
     * overlap
     * check bank is mainantenance by bankfunction
     *
     * @param pBankCode
     * @return
     */
    public boolean isBankMaintenance(String pBankCode, EBankFunction pBankFunction) {
        BankConfig bankConfig = getBankByBankCode(pBankCode);

        if (bankConfig != null && bankConfig.isBankMaintenence(pBankFunction)) {
            maintenanceBank = bankConfig;
            maintenanceBankFunction = bankConfig.getBankFunction(GlobalData.getCurrentBankFunction());
            return true;
        }

        return false;
    }

    /***
     * is this bank support for transaction
     *
     * @param pBankCode
     * @return
     */
    public boolean isBankSupport(String pBankCode) {
        BankConfig bankConfig = getBankByBankCode(pBankCode);
        if(bankConfig == null)
        {
            return false;
        }
        else if(!bankConfig.isBankActive())
        {
            return false;
        }
        else
        {
            return true;
        }
    }

}
