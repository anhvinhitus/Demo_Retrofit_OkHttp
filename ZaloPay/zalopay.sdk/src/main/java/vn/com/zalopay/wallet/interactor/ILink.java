package vn.com.zalopay.wallet.interactor;

import java.util.List;

import rx.Observable;
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

    List<BankAccount> getBankAccountList(String userid);

    void putBankAccounts(String userid, String checksum, List<BankAccount> bankAccountList);
}
