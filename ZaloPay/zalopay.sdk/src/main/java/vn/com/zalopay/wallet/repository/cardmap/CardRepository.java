package vn.com.zalopay.wallet.repository.cardmap;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;

/**
 * Created by chucvv on 6/7/17.
 */

public class CardRepository implements CardStore.Repository {
    private CardStore.LocalStorage mLocalStorage;
    private CardStore.CardMapService cardMapService;

    public CardRepository(CardStore.CardMapService cardMapService, CardStore.LocalStorage localStorage) {
        this.cardMapService = cardMapService;
        this.mLocalStorage = localStorage;
    }

    @Override
    public Observable<BaseResponse> removeCard(String userid, String accessToken,
                                               String cardname,
                                               String first6cardno, String last4cardno,
                                               String bankCode, String appVersion) {
        return cardMapService.removeMapCard(userid, accessToken, cardname, first6cardno, last4cardno, bankCode, appVersion)
                .doOnSubscribe(() -> mLocalStorage.clearCheckSum())
                .doOnNext(baseResponse -> mLocalStorage.resetMapCardCache(userid, first6cardno, last4cardno));
    }

    @Override
    public Observable<CardInfoListResponse> fetchCloud(String userid, String accesstoken, String checksum, String appversion) {
        return cardMapService.fetch(userid, accesstoken, checksum, appversion)
                .doOnNext(cardInfoListResponse -> mLocalStorage.saveResponse(userid, cardInfoListResponse));
    }

    @Override
    public CardStore.LocalStorage getLocalStorage() {
        return mLocalStorage;
    }
}
