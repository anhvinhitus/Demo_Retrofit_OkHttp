package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetBankListImpl;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class GetBankList extends BaseRequest<BankConfigResponse> {
    protected static boolean mLoading;
    private ILoadBankListListener mILoadBankListListener;

    public GetBankList(ILoadBankListListener pILoadBankListListener) {
        super();
        mILoadBankListListener = pILoadBankListListener;
    }

    public static boolean isLoading() {
        return mLoading;
    }

    @Override
    protected void doRequest() {
        mLoading = true;

        try {
           newDataRepository().getData(new GetBankListImpl(), getDataParams());
        } catch (Exception e) {
            Log.e(this, e);

            onRequestFail(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
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

    protected void saveBankListToCache() throws Exception {
        //save bank list to cache
        long time_to_live = System.currentTimeMillis() + getResponse().expiredtime;
        SharedPreferencesManager.getInstance().setExpiredBankList(time_to_live);
        SharedPreferencesManager.getInstance().setCheckSumBankList(getResponse().checksum);

        //for testing
        /*
        BankConfig bankConfigvietcombank = new BankConfig();
		bankConfigvietcombank.allowwithdraw = 1;
		bankConfigvietcombank.code = GlobalData.getStringResource(RS.string.zpw_string_bankcode_bidv);
		bankConfigvietcombank.status = 1;
		bankConfigvietcombank.supporttype = 1;
		bankConfigvietcombank.name = "NH BIDV";
		getResponse().banklist.add(bankConfigvietcombank);

        getResponse().bankcardprefixmap.put("970418",GlobalData.getStringResource(RS.string.zpw_string_bankcode_bidv));
        */

        for (BankConfig bankConfig : getResponse().banklist) {
            if (bankConfig.code.equals("123PBIDV")) {
                //bankConfig.interfacetype = 1; // = 1 using parse, = 2 using web

                /*
				bankConfig.status = -1;
				bankConfig.maintenanceto = Long.parseLong("1480063794000");
				bankConfig.maintenancemsg = "NH ZPVCB bảo trì tới 21:00:00 07/02/2017, vui lòng chọn ngân hàng khác hoặc quay lại sau";
                */
                //bankConfig.functions.get(2).status = 2;
                //bankConfig.functions.get(0).maintenanceto = Long.parseLong("1480063794000");
                //bankConfig.interfacetype = 1;
            }
            if (bankConfig.code.equals("ZPVCB")) {
                //bankConfig.interfacetype = 1; // = 1 using parse, = 2 using web

				/*
				bankConfig.status = 2;
				bankConfig.maintenanceto = Long.parseLong("1480063794000");
				bankConfig.maintenancemsg = "NH ZPVCB bảo trì tới 21:00:00 07/02/2017, vui lòng chọn ngân hàng khác hoặc quay lại sau";
				*/

                //bankConfig.functions.get(2).status = 2;
                //bankConfig.functions.get(0).maintenanceto = Long.parseLong("1480063794000");
            }

            if (bankConfig.code.equals("123PSGCB")) {

				/*
				bankConfig.status = 2;
				bankConfig.maintenanceto = Long.parseLong("1480063794000");
				bankConfig.maintenancemsg = "NH 123PSGCB bảo trì tới 21:00:00 07/02/2017, vui lòng chọn ngân hàng khác hoặc quay lại sau";
				*/

                //bankConfig.functions.get(0).status = 2;
                //bankConfig.functions.get(0).maintenanceto = Long.parseLong("1480063794000");
            }

            if (bankConfig.code.equals("123PVTB")) {
                //bankConfig.status = 1;
                //bankConfig.maintenanceto = Long.parseLong("1480063794000");
            }


            SharedPreferencesManager.getInstance().setBankConfig(bankConfig.code, GsonUtils.toJsonString(bankConfig));
        }

        String hashMapBank = GsonUtils.toJsonString(getResponse().bankcardprefixmap);
        SharedPreferencesManager.getInstance().setBankConfigMap(hashMapBank);
        BankCardCheck.mBankMap = getResponse().bankcardprefixmap;

        mLoading = false;
        if (mILoadBankListListener != null) {
            mILoadBankListListener.onComplete();
        }
    }

    protected void loadBankListFromCache() throws Exception {
        //update timeout bank list
        long time_to_live = System.currentTimeMillis() + getResponse().expiredtime;
        SharedPreferencesManager.getInstance().setExpiredBankList(time_to_live);

        java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        HashMap<String, String> bankMap = GsonUtils.fromJsonString(SharedPreferencesManager.getInstance().getBankMap(), type);

        BankCardCheck.mBankMap = bankMap;

        mLoading = false;
        if (mILoadBankListListener != null) {
            mILoadBankListListener.onComplete();
        }
    }

    @Override
    protected void onRequestSuccess() throws Exception {

        if (!(getResponse() instanceof BankConfigResponse)) {
            onRequestFail(GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
            return;
        }

        if (getResponse().returncode < 1) {
            onRequestFail(getResponse().getMessage());
            return;
        }

        if (isChangedCheckSumBankList(getResponse().checksum)) {
            Log.d(this, "===saving banklist to cache");
            saveBankListToCache();
        } else {
            //dont have any new on api, so need to get again from cache.
            Log.d(this, "===same same check sum , no need to update cache again");

            loadBankListFromCache();
        }
    }

    @Override
    protected void onRequestFail(String pMessage) {
        mLoading = false;
        if (mILoadBankListListener != null) {
            mILoadBankListListener.onError(!TextUtils.isEmpty(pMessage) ? pMessage : GlobalData.getStringResource(RS.string.zingpaysdk_alert_network_error));
        }
    }

    @Override
    protected void onRequestInProcess() {
        if (mILoadBankListListener != null) {
            mILoadBankListListener.onProcessing();
        }
    }

    @Override
    protected boolean doParams() {
        try {
            String pCheckSum = SharedPreferencesManager.getInstance().getCheckSumBankList();
            DataParameter.prepareGetBankList(mDataParams, pCheckSum);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }
        return true;
    }
}
