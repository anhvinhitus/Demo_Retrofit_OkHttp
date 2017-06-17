package vn.com.zalopay.wallet.repository.cardmap;

import android.text.TextUtils;

import java.util.List;

import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

/**
 * Created by chucvv on 6/7/17.
 */

public class CardLocalStorage extends AbstractLocalStorage implements CardStore.LocalStorage {
    public CardLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        super(sharedPreferencesManager);
    }

    @Override
    public String getCheckSum() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getCardInfoCheckSum();
        } catch (Exception e) {
            Log.e(this, e);
        }
        return !TextUtils.isEmpty(checksum) ? checksum : "";
    }

    @Override
    public String getCardKeyList(String userid) {
        String cardKeyList = null;
        try {
            cardKeyList = mSharedPreferences.getMapCardKeyList(userid);
        } catch (Exception e) {
            Log.e(this, e);
        }
        return !TextUtils.isEmpty(cardKeyList) ? cardKeyList : "";
    }

    @Override
    public void setCard(String userid, BaseMap card) {
        if (card == null) {
            return;
        }
        String cardKey = card.getKey();
        mSharedPreferences.setMap(userid, cardKey, GsonUtils.toJsonString(card));
    }

    @Override
    public MapCard getCard(String userid, String cardKey) {
        String map = mSharedPreferences.getMap(userid, cardKey);
        MapCard mapCard = null;
        if (!TextUtils.isEmpty(map)) {
            mapCard = GsonUtils.fromJsonString(map, MapCard.class);
        }
        return mapCard;
    }

    @Override
    public void setCardKeyList(String userid, String cardKeyList) {
        mSharedPreferences.setMapCardList(userid, cardKeyList);
    }

    public boolean needUpdate(String newCheckSum) {
        try {
            String checkSumOnCache = getCheckSum();
            if (TextUtils.isEmpty(checkSumOnCache) || (!TextUtils.isEmpty(checkSumOnCache) && !checkSumOnCache.equals(newCheckSum))) {
                return true;
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
        return false;
    }

    @Override
    public void put(String pUserId, String checkSum, List<MapCard> cardList) {
        if (!needUpdate(checkSum)) {
            return;
        }
        try {
            mSharedPreferences.setCardInfoCheckSum(checkSum);
            if (cardList != null && cardList.size() > 0) {
                StringBuilder mappCardID = new StringBuilder();
                int count = 0;
                for (BaseMap card : cardList) {
                    count++;
                    setCard(pUserId, card);
                    mappCardID.append(card.getKey());
                    if (count < cardList.size()) {
                        mappCardID.append(Constants.COMMA);
                    }
                }
                //key map list
                setCardKeyList(pUserId, mappCardID.toString());
                Log.d(this, "save map card list", mappCardID.toString());
            } else {
                //clear map card list
                mSharedPreferences.resetMapCardListOnCache(pUserId);
                Log.d(this, "clear map card list");
            }
        } catch (Exception e) {
            Log.e(this, e);
        }
    }

    @Override
    public void saveResponse(String pUserId, CardInfoListResponse pResponse) {
        Log.d(this, "start save card info to cache", pResponse);
        if (pResponse == null || pResponse.returncode != 1) {
            Log.d(this, "stop save card info cache because result fail");
            return;
        }
        put(pUserId, pResponse.cardinfochecksum, pResponse.cardinfos);
    }


    @Override
    public void clearCheckSum() {
        mSharedPreferences.setCardInfoCheckSum(null);
    }
}