package vn.com.zalopay.wallet.interactor;

import java.util.List;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;

/**
 * Created by chucvv on 6/10/17.
 * Declare methods for managing user link source of funds
 */

public interface ILinkSourceInteractor {
    /**
     * Get list of link cards (ATM cards)
     */
    Observable<Boolean> getCards(String userid, String accesstoken, boolean pReload, String appversion);

    /**
     * Get list of link bank accounts
     */
    Observable<Boolean> getBankAccounts(String userid, String accesstoken, boolean pReload, String appversion);

    Observable<Boolean> getMap(String userid, String accesstoken, boolean pReload, String appversion);

    void putCards(String userid, String checksum, List<MapCard> cardList);

    void putCard(String userid, MapCard mapCard);

    void putCardNumber(String cardNumber);

    String getCardNumber();

    MapCard getCard(String userid, String cardKey);

    List<BankAccount> getBankAccountList(String userid);

    List<MapCard> getMapCardList(String pUserID);

    Observable<Boolean> refreshMapList(String appVersion, String userId, String accessToken, String first6cardno, String last4cardno);

    void putBankAccounts(String userid, String checksum, List<BankAccount> bankAccountList);

    void clearCheckSum();

    Observable<BaseResponse> removeMap(String userid, String accessToken, String cardname, String first6cardno, String last4cardno, String bankCode, String appVersion);
}
