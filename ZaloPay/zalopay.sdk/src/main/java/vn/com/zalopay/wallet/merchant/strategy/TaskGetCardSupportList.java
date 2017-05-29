package vn.com.zalopay.wallet.merchant.strategy;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import timber.log.Timber;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class TaskGetCardSupportList extends TaskBase {
    /***
     * load bank list listener
     */
    private ILoadBankListListener mLoadBankListListener = new ILoadBankListListener() {
        @Override
        public void onProcessing() {
            onTaskInProcess();
        }

        @Override
        public void onComplete() {
            ArrayList<ZPCard> cardArrayList = populateCardSuportList();

            if (getListener() instanceof IGetCardSupportListListener) {
                Log.d(this, "===onComplete===callback to app", cardArrayList);
                ((IGetCardSupportListListener) getListener()).onComplete(cardArrayList);
            } else {
                Log.d(this, "====onComplete====getListener()=NULL");
            }
        }

        @Override
        public void onError(String pMessage) {
            onTaskError(pMessage);
        }
    };

    @Override
    protected void onDoIt() {
        BankLoader.loadBankList(mLoadBankListListener);
    }

    protected String getCardBitmapName(String pBankCode) {
        return String.format("bank_%s%s", pBankCode, Constants.BITMAP_EXTENSION);
    }

    protected ArrayList<ZPCard> populateCardSuportList() {
        ArrayList<ZPCard> cardArrayList = new ArrayList<>();

        //cc must be hardcode

        String bankCodeVisa = GlobalData.getStringResource(RS.string.zpw_string_bankcode_visa);
        String bankNameVisa = GlobalData.getStringResource(RS.string.zpw_string_bankname_visa);
        String bankCodeMaster = GlobalData.getStringResource(RS.string.zpw_string_bankcode_master);
        String bankNameMaster = GlobalData.getStringResource(RS.string.zpw_string_bankname_master);
        if (!TextUtils.isEmpty(bankCodeVisa) && !TextUtils.isEmpty(bankNameVisa)) {
            ZPCard zpCard = new ZPCard(bankCodeVisa, getCardBitmapName(bankCodeVisa), bankNameVisa);
            cardArrayList.add(zpCard);
        }
        if (!TextUtils.isEmpty(bankCodeMaster) && !TextUtils.isEmpty(bankNameMaster)) {
            ZPCard zpCard = new ZPCard(bankCodeMaster, getCardBitmapName(bankCodeMaster), bankNameMaster);
            cardArrayList.add(zpCard);
        }

        //load bank
        if (BankCardCheck.mBankMap != null) {
            Iterator it = BankCardCheck.mBankMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();

                String bankCode = String.valueOf(pair.getValue());

                if (!TextUtils.isEmpty(bankCode)) {
                    boolean isBankAccount = BankAccountHelper.isBankAccount(bankCode);
                    ZPCard zpCard = new ZPCard(bankCode, getCardBitmapName(bankCode), isBankAccount, getBankName(bankCode));

                    if (!cardArrayList.contains(zpCard))
                        cardArrayList.add(zpCard);
                }
            }
        }

        Log.d(this, "===cardSupportHashMap.size()=" + cardArrayList.size());

        return cardArrayList;
    }

    private String getBankName(String bankCode) {
        if (TextUtils.isEmpty(bankCode)) {
            return "";
        }
        String strBankConfig = "";
        try {
            strBankConfig = SharedPreferencesManager.getInstance().getBankConfig(bankCode);
        } catch (Exception e) {
            Timber.w(e, "Function getBankName throw exception [%s]", e.getMessage());
        }
        if (TextUtils.isEmpty(strBankConfig)) {
            return "";
        }
        BankConfig bankConfig = GsonUtils.fromJsonString(strBankConfig, BankConfig.class);
        if (bankConfig == null || TextUtils.isEmpty(bankConfig.name)) {
            return "";
        }
        if (bankConfig.name.startsWith("NH")) {
            return bankConfig.name.substring(2).trim();
        } else {
            return bankConfig.name.trim();
        }
    }
}
