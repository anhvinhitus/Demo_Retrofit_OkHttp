package vn.com.zalopay.wallet.merchant.strategy;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
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
                Log.d(this, "===onComplete===callback to app" + GsonUtils.toJsonString(cardArrayList));
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
        String bankCodeMaster = GlobalData.getStringResource(RS.string.zpw_string_bankcode_master);
        String bankCodeJcb = GlobalData.getStringResource(RS.string.zpw_string_bankcode_jcb);

        if (!TextUtils.isEmpty(bankCodeVisa)) {
            ZPCard zpCard = new ZPCard(bankCodeVisa, getCardBitmapName(bankCodeVisa));
            cardArrayList.add(zpCard);
        }
        if (!TextUtils.isEmpty(bankCodeMaster)) {
            ZPCard zpCard = new ZPCard(bankCodeMaster, getCardBitmapName(bankCodeMaster));
            cardArrayList.add(zpCard);
        }
        if (!TextUtils.isEmpty(bankCodeJcb)) {
            ZPCard zpCard = new ZPCard(bankCodeJcb, getCardBitmapName(bankCodeJcb));
            cardArrayList.add(zpCard);
        }

        //load bank
        if (BankLoader.mapBank != null) {
            Iterator it = BankLoader.mapBank.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String bankCode = String.valueOf(pair.getValue());
                if (!TextUtils.isEmpty(bankCode)) {
                    boolean isBankAccount = BankAccountHelper.isBankAccount(bankCode);
                    ZPCard zpCard = new ZPCard(bankCode, getCardBitmapName(bankCode), isBankAccount);

                    if (!cardArrayList.contains(zpCard))
                        cardArrayList.add(zpCard);
                }
            }
        }

        Log.d(this, "===cardSupportHashMap.size()=" + cardArrayList.size());
        return cardArrayList;
    }
}
