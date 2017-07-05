package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import timber.log.Timber;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.repository.bankaccount.BankAccountStore;
import vn.com.zalopay.wallet.repository.cardmap.CardStore;

/**
 * Created by chucvv on 6/10/17.
 */

public class LinkSourceInteractor implements ILinkSourceInteractor {
    private CardStore.Repository cardRepository;
    private BankAccountStore.Repository bankAccountRepository;

    @Inject
    public LinkSourceInteractor(CardStore.Repository cardRepository, BankAccountStore.Repository bankAccountRepository) {
        this.cardRepository = cardRepository;
        this.bankAccountRepository = bankAccountRepository;
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
        this.bankAccountRepository.getLocalStorage().resetBankAccountCacheList(userId);
        this.cardRepository.getLocalStorage().resetMapCardCacheList(userId);
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
        Observable<BaseResponse> removeMapObservable = cardRepository
                .removeCard(userid, accessToken, cardname, first6cardno, last4cardno, bankCode, appVersion)
                .onErrorReturn(throwable -> {
                    BaseResponse baseResponse = new BaseResponse();
                    baseResponse.returncode = -1;
                    baseResponse.returnmessage = GlobalData.getStringResource(RS.string.zpw_alert_network_error_removemapcard);
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
            this.cardRepository.getLocalStorage().resetMapCardCache(userId, first6cardno, last4cardno);
            return getCards(userId, accessToken, true, appVersion);
        } else if (bankAccount != null && !TextUtils.isEmpty(bankAccount.bankcode)) {
            this.bankAccountRepository.getLocalStorage().resetBankAccountCache(userId, first6cardno, last4cardno);
            return getBankAccounts(userId, accessToken, true, appVersion);
        } else {
            return refreshAll(appVersion, userId, accessToken);
        }
    }

    @Override
    public List<BankAccount> getBankAccountList(String userid) {
        return this.bankAccountRepository.getLocalStorage().getBankAccountList(userid);
    }

    @Override
    public Observable<Boolean> getCards(String userid, String accesstoken, boolean pReload, String appversion) {
        String checksum = "";
        if (pReload) {
            cardRepository.getLocalStorage().clearCheckSum();
        } else {
            checksum = cardRepository.getLocalStorage().getCheckSum();
        }
        return cardRepository.fetchCloud(userid, accesstoken, checksum, appversion)
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
            if (pReload) {
                bankAccountRepository.getLocalStorage().clearCheckSum();
            }
        } else {
            checksum = bankAccountRepository.getLocalStorage().getCheckSum();
        }
        return bankAccountRepository.fetchCloud(userid, accesstoken, checksum, appversion)
                .flatMap(new Func1<BankAccountListResponse, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BankAccountListResponse bankAccountListResponse) {
                        return Observable.just(true);
                    }
                });
    }

    @Override
    public Observable<Boolean> getMap(String userid, String accesstoken, boolean pReload, String appversion) {
        Observable<Boolean> mapObservable = Observable.zip(getCards(userid, accesstoken, pReload, appversion),
                getBankAccounts(userid, accesstoken, pReload, appversion),
                (finishLoadCard, finishLoadBankAccount) -> finishLoadCard && finishLoadBankAccount);
        return Observable.concat(expireObservable(),mapObservable)
                .first(stopStream -> stopStream);
    }

    @Override
    public void putCards(String userid, String checksum, List<MapCard> cardList) {
        cardRepository.getLocalStorage().put(userid, checksum, cardList);
    }

    @Override
    public void putBankAccounts(String userid, String checksum, List<BankAccount> bankAccountList) {
        bankAccountRepository.getLocalStorage().put(userid, checksum, bankAccountList);
    }

    @Override
    public MapCard getCard(String userid, String cardKey) {
        return cardRepository.getLocalStorage().getCard(userid, cardKey);
    }

    private BankAccount getBankAccount(String userid, String key) {
        return bankAccountRepository.getLocalStorage().getBankAccount(userid, key);
    }

    private Observable<Boolean> expireObservable(){
        boolean expire = cardRepository.getLocalStorage().expireTime() > System.currentTimeMillis();
        return Observable.just(expire);
    }
}
