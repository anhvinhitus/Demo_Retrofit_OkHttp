package vn.com.zalopay.wallet.repository.cardmap;

import android.text.TextUtils;

import java.util.List;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.helper.ListUtils;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;
import vn.com.zalopay.wallet.repository.SharedPreferencesManager;

/**
 * Created by chucvv on 6/7/17.
 */

public class CardLocalStorage extends AbstractLocalStorage implements CardStore.LocalStorage {
    public CardLocalStorage(SharedPreferencesManager sharedPreferencesManager) {
        super(sharedPreferencesManager);
    }

    @Override
    public void resetMapCardCache(String userId, String first6cardno, String last4cardno) {
        String cardKey = first6cardno + last4cardno;
        mSharedPreferences.setMap(userId, cardKey, null);
        String keyList = getCardKeyList(userId);
        if (TextUtils.isEmpty(keyList)) {
            return;
        }
        String cardKeyList = ListUtils.filterMapKey(keyList, cardKey);
        setCardKeyList(userId, cardKeyList);
    }

    @Override
    public void resetMapCardCacheList(String userId) {
        mSharedPreferences.resetMapCardListCache(userId);
    }

    @Override
    public String getCheckSum() {
        String checksum = null;
        try {
            checksum = mSharedPreferences.getCardInfoCheckSum();
        } catch (Exception e) {
            Timber.d(e, "Exception getCheckSum");
        }
        return !TextUtils.isEmpty(checksum) ? checksum : "";
    }

    @Override
    public String getCardKeyList(String userid) {
        String cardKeyList = null;
        try {
            cardKeyList = mSharedPreferences.getMapCardKeyList(userid);
        } catch (Exception e) {
            Timber.d(e, "Exception getCardKeyList");
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
        if (TextUtils.isEmpty(map)) {
            return null;
        }

        return GsonUtils.fromJsonString(map, MapCard.class);
    }

    @Override
    public void setCardKeyList(String userid, String cardKeyList) {
        mSharedPreferences.setMapCardList(userid, cardKeyList);
    }

    private boolean needUpdate(String newCheckSum) {
        try {
            String checkSumOnCache = getCheckSum();
            if (TextUtils.isEmpty(checkSumOnCache)) {
                return true;
            }
            if (!checkSumOnCache.equals(newCheckSum)) {
                return true;
            }
        } catch (Exception e) {
            Timber.d(e, "Exception needUpdate");
        }
        return false;
    }

    @Override
    public void put(String pUserId, String checkSum, List<MapCard> cardList) {
        if (!needUpdate(checkSum)) {
            Timber.d("map card list cache is valid - skip update");
            return;
        }
        Timber.d("start update map card list on cache");
        mSharedPreferences.setCardInfoCheckSum(checkSum);
        if (cardList == null || cardList.size() <= 0) {
            Timber.d("clear map card list");
            resetMapCardCacheList(pUserId);
            return;
        }
        StringBuilder keyListBuilder = new StringBuilder();
        int count = 0;
        for (BaseMap card : cardList) {
            count++;
            setCard(pUserId, card);
            keyListBuilder.append(card.getKey());
            if (count < cardList.size()) {
                keyListBuilder.append(Constants.COMMA);
            }
        }
        //key map list
        setCardKeyList(pUserId, keyListBuilder.toString());
    }

    @Override
    public void saveResponse(String pUserId, CardInfoListResponse pResponse) {
        if (pResponse == null || pResponse.returncode != 1) {
            Timber.d("stop save card info cache because result fail");
            return;
        }
        put(pUserId, pResponse.cardinfochecksum, pResponse.cardinfos);
    }


    @Override
    public void clearCheckSum() {
        mSharedPreferences.setCardInfoCheckSum(null);
    }

    @Override
    public SharedPreferencesManager sharePref() {
        return mSharedPreferences;
    }
}