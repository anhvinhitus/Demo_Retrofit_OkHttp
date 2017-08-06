package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.repository.bankaccount.BankAccountStore;
import vn.com.zalopay.wallet.repository.cardmap.CardStore;
import vn.com.zalopay.wallet.tracker.ZPAnalyticsTrackerWrapper;

/**
 * Created by chucvv on 6/10/17.
 */

public class LinkSourceInteractor implements ILinkSourceInteractor {
    private CardStore.CardMapService mCardMapService;
    private CardStore.LocalStorage mCardMapLocalStorage;
    private BankAccountStore.BankAccountService mBankAccountService;
    private BankAccountStore.LocalStorage mBankAccountLocalStorage;

    @Inject
    public LinkSourceInteractor(CardStore.CardMapService cardMapService, CardStore.LocalStorage cardMapLocalStorage,
                                BankAccountStore.BankAccountService bankAccountService, BankAccountStore.LocalStorage bankAccountLocalStorage) {
        this.mCardMapService = cardMapService;
        this.mCardMapLocalStorage = cardMapLocalStorage;
        this.mBankAccountService = bankAccountService;
        this.mBankAccountLocalStorage = bankAccountLocalStorage;
    }

    @Override
    public Observable<Boolean> refreshMapList(String appVersion, String userId, String accessToken, String first6cardno, String last4cardno) {
        if (TextUtils.isEmpty(first6cardno) || TextUtils.isEmpty(last4cardno)) {
            return refreshAll(appVersion, userId, accessToken);
        } else {
            return refreshMap(appVersion, userId, accessToken, first6cardno, last4cardno);
        }
    }

    private Observable<Boolean> refreshAll(String appVersion, String userId, String accessToken) {
        this.mBankAccountLocalStorage.resetBankAccountCacheList(userId);
        this.mCardMapLocalStorage.resetMapCardCacheList(userId);
        return getMap(userId, accessToken, true, appVersion);
    }

    @Override
    public Observable<BaseResponse> removeMap(String userid, String accessToken, String cardname, String first6cardno, String last4cardno, String bankCode, String appVersion) {
        Observable<BaseResponse> reloadCardObservable = getCards(userid, accessToken, true, accessToken)
                .flatMap(new Func1<Boolean, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(Boolean aBoolean) {
                        BaseResponse baseResponse = new BaseResponse();
                        baseResponse.returncode = 0;
                        return Observable.just(baseResponse);
                    }
                });
        Observable<BaseResponse> removeMapObservable = mCardMapService
                .removeMapCard(userid, accessToken, cardname, first6cardno, last4cardno, bankCode, appVersion)
                .doOnSubscribe(() -> mCardMapLocalStorage.clearCheckSum())
                .doOnNext(baseResponse -> mCardMapLocalStorage.resetMapCardCache(userid, first6cardno, last4cardno))
                .onErrorReturn(throwable -> {
                    BaseResponse baseResponse = new BaseResponse();
                    baseResponse.returncode = -1;
                    baseResponse.returnmessage = GlobalData.getAppContext().getResources().getString(R.string.sdk_error_networking_removemapcard_mess);
                    return baseResponse;
                });
        return Observable.concat(removeMapObservable, reloadCardObservable).first(baseResponse -> baseResponse != null && baseResponse.returncode != 1);
    }

    /***
     * refesh map list depend on bank code
     */
    private Observable<Boolean> refreshMap(String appVersion, String userId, String accessToken, String first6cardno, String last4cardno) {
        String key = first6cardno + last4cardno;
        MapCard mapCard = getCard(userId, key);
        BankAccount bankAccount = getBankAccount(userId, key);
        if (mapCard != null && !TextUtils.isEmpty(mapCard.bankcode)) {
            mCardMapLocalStorage.resetMapCardCache(userId, first6cardno, last4cardno);
            return getCards(userId, accessToken, true, appVersion);
        } else if (bankAccount != null && !TextUtils.isEmpty(bankAccount.bankcode)) {
            mBankAccountLocalStorage.resetBankAccountCache(userId, first6cardno, last4cardno);
            return getBankAccounts(userId, accessToken, true, appVersion);
        } else {
            return refreshAll(appVersion, userId, accessToken);
        }
    }

    @Override
    public List<BankAccount> getBankAccountList(String userid) {
        return mBankAccountLocalStorage.getBankAccountList(userid);
    }

    @Override
    public Observable<Boolean> getCards(String userid, String accesstoken, boolean pReload, String appversion) {
        String checksum = "";
        if (pReload) {
            mCardMapLocalStorage.clearCheckSum();
        } else {
            checksum = mCardMapLocalStorage.getCheckSum();
        }
        long startTime = System.currentTimeMillis();
        int apiId = ZPEvents.API_UM_LISTCARDINFOFORCLIENT;
        return mCardMapService.fetch(userid, accesstoken, checksum, appversion)
                .doOnError(throwable -> ZPAnalyticsTrackerWrapper.trackApiError(apiId, startTime, throwable))
                .doOnNext(cardInfoListResponse -> mCardMapLocalStorage.saveResponse(userid, cardInfoListResponse))
                .doOnNext(cardInfoListResponse -> ZPAnalyticsTrackerWrapper.trackApiCall(apiId, startTime, cardInfoListResponse))
                .flatMap(new Func1<CardInfoListResponse, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(CardInfoListResponse cardInfoListResponse) {
                        return Observable.just(true);
                    }
                });
    }

    @Override
    public Observable<Boolean> getBankAccounts(String userid, String accesstoken, boolean pReload, String appversion) {
        String checksum = "";
        if (pReload) {
            mBankAccountLocalStorage.clearCheckSum();
        } else {
            checksum = mBankAccountLocalStorage.getCheckSum();
        }
        long startTime = System.currentTimeMillis();
        int apiId = ZPEvents.API_UM_LISTBANKACCOUNTFORCLIENT;
        return mBankAccountService.fetch(userid, accesstoken, checksum, appversion)
                .doOnError(throwable -> ZPAnalyticsTrackerWrapper.trackApiError(apiId, startTime, throwable))
                .doOnNext(bankAccountListResponse -> mBankAccountLocalStorage.saveResponse(userid, bankAccountListResponse))
                .doOnNext(bankAccountListResponse -> ZPAnalyticsTrackerWrapper.trackApiCall(apiId, startTime, bankAccountListResponse))
                .flatMap(new Func1<BankAccountListResponse, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BankAccountListResponse bankAccountListResponse) {
                        return Observable.just(true);
                    }
                });
    }

    @Override
    public Observable<Boolean> getMap(String userid, String accesstoken, boolean pReload, String appversion) {
        return Observable.zip(getCards(userid, accesstoken, pReload, appversion),
                getBankAccounts(userid, accesstoken, pReload, appversion),
                (finishLoadCard, finishLoadBankAccount) -> finishLoadCard && finishLoadBankAccount);
    }

    @Override
    public void putCards(String userid, String checksum, List<MapCard> cardList) {
        mCardMapLocalStorage.put(userid, checksum, cardList);
    }

    @Override
    public void putBankAccounts(String userid, String checksum, List<BankAccount> bankAccountList) {
        mBankAccountLocalStorage.put(userid, checksum, bankAccountList);
    }

    @Override
    public MapCard getCard(String userid, String cardKey) {
        return mCardMapLocalStorage.getCard(userid, cardKey);
    }

    private BankAccount getBankAccount(String userid, String key) {
        return mBankAccountLocalStorage.getBankAccount(userid, key);
    }

    @Override
    public void clearCheckSum() {
        mCardMapLocalStorage.clearCheckSum();
    }

    @Override
    public List<MapCard> getMapCardList(String pUserID) {
        return mCardMapLocalStorage.sharePref().getMapCardList(pUserID);
    }

    @Override
    public void putCard(String userId, MapCard mapCard) {
        String mapCardKeyList = mCardMapLocalStorage.sharePref().getMapCardKeyList(userId);
        if (TextUtils.isEmpty(mapCardKeyList)) {
            mapCardKeyList = mapCard.getKey();
        } else if (!mapCardKeyList.contains(mapCard.getKey())) {
            mapCardKeyList += (Constants.COMMA + mapCard.getKey());
        }
        mCardMapLocalStorage.setCard(userId, mapCard);
        mCardMapLocalStorage.setCardKeyList(userId, mapCardKeyList);
    }
}
