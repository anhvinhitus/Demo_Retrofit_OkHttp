package vn.com.zalopay.wallet.repository.bankaccount;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankAccountStore {
    public interface LocalStorage {
        void saveResponse(String pUserId, BankAccountListResponse bankAccountListResponse);

        void put(String pUserId, String checkSum, List<BankAccount> bankAccountList);

        String getCheckSum();

        List<BankAccount> getBankAccountList(String userid);

        void setAccount(String userid, BaseMap bankAccount);

        void setKeyList(String userid, String keyList);

        void clearCheckSum();
    }

    public interface Repository {
        Observable<BankAccountListResponse> fetchCloud(String userid, String accesstoken, String checksum, String appversion);

        BankAccountStore.LocalStorage getLocalStorage();
    }

    public interface BankAccountService {
        @GET(Constants.URL_LISTBANKACCCOUNT)
        @API_NAME(value = {ZPEvents.API_UM_LISTBANKACCOUNTFORCLIENT, ZPEvents.CONNECTOR_UM_LISTBANKACCOUNTFORCLIENT})
        Observable<BankAccountListResponse> fetch(@Query("userid") String userid, @Query("accesstoken") String accesstoken,
                                                  @Query("bankaccountchecksum") String bankaccountchecksum, @Query("appversion") String appversion);
    }
}
