package vn.com.zalopay.wallet.business.behavior.gateway;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankFunction;
import vn.com.zalopay.wallet.business.entity.enumeration.EBankFunction;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.datasource.request.BankListTask;
import vn.com.zalopay.wallet.datasource.request.BaseTask;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class BankLoader extends SingletonBase {
    public static Map<String, String> mapBank;
    private static BankLoader _object;
    public BankConfig maintenanceBank;
    public BankFunction maintenanceBankFunction;

    public BankLoader() {
        super();
        maintenanceBank = null;
        maintenanceBankFunction = null;
    }
    public static synchronized BankLoader getInstance() {
        if (BankLoader._object == null) {
            BankLoader._object = new BankLoader();
        }
        return BankLoader._object;
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

    public static boolean existedBankListOnMemory() {
        return (mapBank != null && mapBank.size() > 0);
    }

    public static synchronized void loadBankList(ILoadBankListListener pListener) {
        try {
            if (isNeedToLoadBankList()) {
                BaseTask baseTask = new BankListTask(pListener);
                baseTask.makeRequest();
                return;
            }
            //bank list is on memory now
            else if (existedBankListOnMemory()) {
                Log.d("loadBankList", "===bank list is cached on memory===");
                if (pListener != null) {
                    pListener.onComplete();
                }
            } else {
                Log.d("loadBankList", "===reload banklist from ache===");
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                HashMap<String, String> bankMap = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankMap(), type);
                BankLoader.mapBank = bankMap;
                if (pListener != null) {
                    pListener.onComplete();
                }
            }
        } catch (Exception e) {
            if (pListener != null) {
                pListener.onError(null);
            }
            Log.e("loadBankList", e != null ? e.getMessage() : "error");
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
     * check bank is mainantenance by bankfunction
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
     * @param pBankCode
     * @return
     */
    public boolean isBankSupport(String pBankCode) {
        BankConfig bankConfig = getBankByBankCode(pBankCode);
        return bankConfig != null && bankConfig.isBankActive();
    }

}
