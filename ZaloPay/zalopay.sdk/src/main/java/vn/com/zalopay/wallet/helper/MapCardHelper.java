package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import java.util.List;

import rx.Single;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.base.DMapCardResult;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.datasource.task.MapCardListTask;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class MapCardHelper {
    private static final String TAG = MapCardHelper.class.getCanonicalName();
    /***
     * reload map card list info
     */
    public static Single<BaseResponse> loadMapCardList(boolean pReload) {
        return Single.create(subscriber -> {
            try {
                if (pReload) {
                    SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
                }
                BaseTask tGetCardInfoList = new MapCardListTask(pResponse -> {
                    subscriber.onSuccess(pResponse);
                });
                tGetCardInfoList.makeRequest();
            } catch (Exception e) {
                subscriber.onError(e);
                Log.e("loadMapCardList", e);
            }
        });
    }

    public static boolean needUpdateMapCardListOnCache(String pCardInfoCheckSum) {
        try {
            String cardInfoCheckSumOnCache = SharedPreferencesManager.getInstance().getCardInfoCheckSum();
            if (TextUtils.isEmpty(cardInfoCheckSumOnCache) || (!TextUtils.isEmpty(cardInfoCheckSumOnCache) && !cardInfoCheckSumOnCache.equals(pCardInfoCheckSum))) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return false;
    }

    /***
     * card info list response have new map card
     * @param pCardInfoResponse
     * @return
     */
    public static boolean isGetMapCardInfoSuccessAndHaveNewMapCard(CardInfoListResponse pCardInfoResponse) {
        return pCardInfoResponse != null && pCardInfoResponse.returncode == 1 && pCardInfoResponse.cardinfos != null && pCardInfoResponse.cardinfos.size() > 0;
    }

    /***
     * save map card list to cache
     * @param pCheckSum
     * @param pMapCardList
     * @throws Exception
     */
    public static void saveMapCardListToCache(String pCheckSum, List<DMappedCard> pMapCardList) throws Exception {
        SharedPreferencesManager.getInstance().setCardInfoCheckSum(pCheckSum);
        Log.d(TAG, "saved card info list checksum " + pCheckSum);
        if (pMapCardList != null && pMapCardList.size() > 0) {
            StringBuilder mappedCardID = new StringBuilder();
            int count = 0;
            for (DBaseMap mappedCard : pMapCardList) {
                count++;
                SharedPreferencesManager.getInstance().setMapCard(mappedCard.getCardKey(), GsonUtils.toJsonString(mappedCard));
                mappedCardID.append(mappedCard.getCardKey());
                if (count < pMapCardList.size()) {
                    mappedCardID.append(Constants.COMMA);
                }
            }
            //cache map list
            SharedPreferencesManager.getInstance().setMapCardList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, mappedCardID.toString());
            Log.d(TAG, "saved map card list ids " + mappedCardID.toString());
        } else {
            //clear map card list
            SharedPreferencesManager.getInstance().resetMapCardListOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
            Log.d(TAG, "cleared map card");
        }
    }

    public static void notifyNewMapCardToApp(DMappedCard saveCardInfo) {
        Log.d("notifyNewMapCardToApp", "===saveCardInfo=" + GsonUtils.toJsonString(saveCardInfo));
        DMapCardResult mapCardResult = new DMapCardResult();
        mapCardResult.setLast4Number(saveCardInfo.last4cardno);
        String bankName = null;
        //create icon for map card.
        //this is atm
        if (!TextUtils.isEmpty(saveCardInfo.bankcode) && !saveCardInfo.bankcode.equals(Constants.CCCode)) {
            mapCardResult.setCardLogo(CChannelHelper.makeCardIconNameFromBankCode(saveCardInfo.bankcode));
            //populate channel name
            bankName = BankCardCheck.getInstance().getDetectedBankName();
            if (!TextUtils.isEmpty(bankName) && bankName.startsWith("NH")) {
                bankName = bankName.substring(2);
                bankName = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card_atm), bankName);
            } else if (!TextUtils.isEmpty(bankName)) {
                bankName = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card), bankName);
            }
        }
        //cc
        else {
            CreditCardCheck cardCheck = CreditCardCheck.getInstance();
            cardCheck.detectCard(saveCardInfo.first6cardno);
            if (cardCheck.isDetected()) {
                ECardType cardType = ECardType.fromString(cardCheck.getCodeBankForVerify());
                mapCardResult.setCardLogo(CChannelHelper.makeCardIconNameFromBankCode(cardType.toString()));
                bankName = String.format(GlobalData.getStringResource(RS.string.zpw_save_credit_card), cardCheck.getDetectedBankName());
            }
        }
        if (TextUtils.isEmpty(bankName)) {
            bankName = GlobalData.getStringResource(RS.string.zpw_save_credit_card_default);
        }
        mapCardResult.setBankName(bankName);
        GlobalData.getPaymentResult().mapCardResult = mapCardResult;
        Log.d("notifyNewMapCardToApp", "===mapCardResult=" + GsonUtils.toJsonString(mapCardResult));
    }
}
