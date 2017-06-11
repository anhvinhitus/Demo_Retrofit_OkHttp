package vn.com.zalopay.wallet.interactor;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.repository.bankaccount.BankAccountStore;
import vn.com.zalopay.wallet.repository.cardmap.CardStore;

/**
 * Created by chucvv on 6/10/17.
 */

public class LinkInteractor implements ILink {
    private CardStore.Repository cardRepository;
    private BankAccountStore.Repository bankAccountRepository;

    @Inject
    public LinkInteractor(CardStore.Repository cardRepository, BankAccountStore.Repository bankAccountRepository) {
        this.cardRepository = cardRepository;
        this.bankAccountRepository = bankAccountRepository;
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
        return Observable.zip(getCards(userid, accesstoken, pReload, appversion),
                getBankAccounts(userid, accesstoken, pReload, appversion),
                (finishLoadCard, finishLoadBankAccount) -> finishLoadCard && finishLoadBankAccount);
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
}
