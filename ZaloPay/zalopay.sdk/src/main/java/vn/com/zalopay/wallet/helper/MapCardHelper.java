package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import java.util.List;

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
import vn.com.zalopay.wallet.datasource.request.BaseRequest;
import vn.com.zalopay.wallet.datasource.request.GetMapCardInfoList;
import vn.com.zalopay.wallet.listener.IGetMapCardInfo;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class MapCardHelper {
    /***
     * save card to cached.
     */
    public static String saveMapCardToCache(String pTranId) throws Exception {
        String strCacheCard = SharedPreferencesManager.getInstance().getCardInfoTransaction(pTranId);

        if (TextUtils.isEmpty(strCacheCard)) {
            return GlobalData.getStringResource(RS.string.zpw_string_alert_card_not_found);
        }

        DMappedCard mappedCard = GsonUtils.fromJsonString(strCacheCard, DMappedCard.class);

        String mappedCardList = SharedPreferencesManager.getInstance().getMapCardKeyList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);

        if (TextUtils.isEmpty(mappedCardList)) {
            mappedCardList = mappedCard.getCardKey();
        } else if (!mappedCardList.contains(mappedCard.getCardKey())) {
            mappedCardList += (Constants.COMMA + mappedCard.getCardKey());
        }

        SharedPreferencesManager.getInstance().setMapCard(mappedCard.getCardKey(), GsonUtils.toJsonString(mappedCard));
        SharedPreferencesManager.getInstance().setMapCardList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, mappedCardList);

        //remove cached temp.
        SharedPreferencesManager.getInstance().setCardInfoTransaction(pTranId, null);

        return null;
    }

    /***
     * reload map card list info
     *
     * @param pReloadMapCardInfoListener
     */
    public static void loadMapCardList(boolean pReload, final IReloadMapInfoListener pReloadMapCardInfoListener) {
        try {
            if (pReload) {
                SharedPreferencesManager.getInstance().setCardInfoCheckSum(null);
            }

            BaseRequest tGetCardInfoList = new GetMapCardInfoList(new IGetMapCardInfo() {
                @Override
                public void onGetCardInfoComplete(BaseResponse pResponse) {
                    if (pResponse instanceof CardInfoListResponse && MapCardHelper.isNeedUpdateMapCardInfoOnCache(((CardInfoListResponse) pResponse).cardinfochecksum)) {
                        try {
                            MapCardHelper.updateMapCardInfoListOnCache(((CardInfoListResponse) pResponse).cardinfochecksum, ((CardInfoListResponse) pResponse).cardinfos);
                        } catch (Exception e) {
                            Log.e("updateMapCardInfoOnCache", e);
                        }

                        if (pReloadMapCardInfoListener != null) {
                            pReloadMapCardInfoListener.onComplete(((CardInfoListResponse) pResponse).cardinfos);
                        }
                    } else if (pResponse != null && pReloadMapCardInfoListener != null) {
                        pReloadMapCardInfoListener.onError(pResponse.getMessage());
                    } else if (pReloadMapCardInfoListener != null) {
                        pReloadMapCardInfoListener.onError(null);
                    }
                }
            });

            tGetCardInfoList.makeRequest();
        } catch (Exception e) {
            if (pReloadMapCardInfoListener != null) {
                pReloadMapCardInfoListener.onError(null);
            }
            Log.e("loadMapCardList", e);
        }
    }

    public static boolean isNeedUpdateMapCardInfoOnCache(String pCardInfoCheckSum) {
        try {
            String cardInfoCheckSumOnCache = SharedPreferencesManager.getInstance().getCardInfoCheckSum();
            if (TextUtils.isEmpty(cardInfoCheckSumOnCache) || (!TextUtils.isEmpty(cardInfoCheckSumOnCache) && !cardInfoCheckSumOnCache.equals(pCardInfoCheckSum))) {
                return true;
            }
        } catch (Exception e) {
            Log.e("isNeedUpdateMapCardInfoOnCache", e);
        }

        return false;
    }

    /***
     * card info list response have new map card
     *
     * @param pCardInfoResponse
     * @return
     */
    public static boolean isGetMapCardInfoSuccessAndHaveNewMapCard(CardInfoListResponse pCardInfoResponse) {
        return pCardInfoResponse != null && pCardInfoResponse.returncode == 1 && pCardInfoResponse.cardinfos != null && pCardInfoResponse.cardinfos.size() > 0;
    }

    /***
     * save map card or bank account list to cache
     *
     * @param pInfoCheckSum
     * @param pMapList
     * @throws Exception
     */
    public static void updateMapCardInfoListOnCache(String pInfoCheckSum, List<DMappedCard> pMapList) throws Exception {
        //update checksum
        SharedPreferencesManager.getInstance().setCardInfoCheckSum(pInfoCheckSum);
        Log.d("updateMapCardInfoListOnCache", "===pCardInfoCheckSum=" + pInfoCheckSum);
        //map card list
        if (pMapList != null && pMapList.size() > 0) {
            StringBuilder mappedCardID = new StringBuilder();
            int count = 0;
            for (DBaseMap mappedCard : pMapList) {
                count++;
                //cache card info
                SharedPreferencesManager.getInstance().setMapCard(mappedCard.getCardKey(), GsonUtils.toJsonString(mappedCard));
                mappedCardID.append(mappedCard.getCardKey());
                if (count < pMapList.size()) {
                    mappedCardID.append(Constants.COMMA);
                }
            }
            //cache map list
            SharedPreferencesManager.getInstance().setMapCardList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, mappedCardID.toString());
            Log.d("updateMapCardInfoListOnCache", "====mappedCardID===" + mappedCardID.toString());
        } else {
            //clear map card list
            SharedPreferencesManager.getInstance().resetMapCardListOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
            Log.d("updateMapCardInfoListOnCache", "===clearing map card===");
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
