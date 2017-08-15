package vn.com.zalopay.wallet.repository.bankaccount;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.BankAccountResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankAccountStore {
    public interface LocalStorage extends AbstractLocalStorage.LocalStorage{
        void saveResponse(String pUserId, BankAccountResponse bankAccountListResponse);

        void put(String pUserId, String checkSum, List<BankAccount> bankAccountList);

        String getCheckSum();

        List<BankAccount> getBankAccountList(String userid);

        void resetBankAccountCache(String userId, String first6cardno, String last4cardno);

        void resetBankAccountCacheList(String userId);

        void setBankAccount(String userid, BaseMap bankAccount);

        BankAccount getBankAccount(String userid, String key);

        void setBankAccountKeyList(String userid, String keyList);

        void clearCheckSum();
    }

    public interface BankAccountService {
        @GET(Constants.URL_LISTBANKACCCOUNT)
        @API_NAME(https = ZPEvents.API_UM_LISTBANKACCOUNTFORCLIENT, connector = ZPEvents.CONNECTOR_UM_LISTBANKACCOUNTFORCLIENT)
        Observable<BankAccountResponse> fetch(@Query("userid") String userid, @Query("accesstoken") String accesstoken,
                                              @Query("bankaccountchecksum") String bankaccountchecksum, @Query("appversion") String appversion);
    }
}
