package vn.com.zalopay.wallet.repository.cardmap;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.api.RetryWithDelay;

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
    public Observable<BaseResponse> removeCard(String userid, String accessToken, String cardname, String first6cardno, String last4cardno, String bankCode, String appVersion) {
        return cardMapService.removeMapCard(userid, accessToken, cardname, first6cardno, last4cardno, bankCode, appVersion)
                .doOnNext(baseResponse -> mLocalStorage.resetMapCardCache(userid, first6cardno, last4cardno));
    }

    @Override
    public Observable<CardInfoListResponse> fetchCloud(String userid, String accesstoken, String checksum, String appversion) {
        return cardMapService.fetch(userid, accesstoken, checksum, appversion)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(cardInfoListResponse -> mLocalStorage.saveResponse(userid, cardInfoListResponse));
    }

    @Override
    public CardStore.LocalStorage getLocalStorage() {
        return mLocalStorage;
    }
}
