package vn.com.zalopay.wallet.repository.cardmap;

import rx.Observable;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.api.RetryWithDelay;

/**
 * Created by chucvv on 6/7/17.
 */

public class CardRepository implements CardStore.Repository {
    protected CardStore.LocalStorage mLocalStorage;
    protected CardStore.CardMapService cardMapService;

    public CardRepository(CardStore.CardMapService cardMapService, CardStore.LocalStorage localStorage) {
        this.cardMapService = cardMapService;
        this.mLocalStorage = localStorage;
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
