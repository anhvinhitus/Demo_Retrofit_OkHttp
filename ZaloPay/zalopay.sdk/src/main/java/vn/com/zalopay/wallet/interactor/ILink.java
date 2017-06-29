package vn.com.zalopay.wallet.interactor;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;

/**
 * Created by chucvv on 6/10/17.
 */

public interface ILink {
    Observable<Boolean> getCards(String userid, String accesstoken, boolean pReload, String appversion);

    Observable<Boolean> getBankAccounts(String userid, String accesstoken, boolean pReload, String appversion);

    Observable<Boolean> getMap(String userid, String accesstoken, boolean pReload, String appversion);

    void putCards(String userid, String checksum, List<MapCard> cardList);

    MapCard getCard(String userid, String cardKey);

    BankAccount getBankAccount(String userid, String key);

    List<BankAccount> getBankAccountList(String userid);

    Subscription refreshMapList(String appVersion, String userId, String accessToken, String first6cardno, String last4cardno);

    void putBankAccounts(String userid, String checksum, List<BankAccount> bankAccountList);
}
