package vn.com.zalopay.wallet.repository.banklist;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;

/**
 * Created by chucvv on 6/7/17.
 */

public class BankListStore {
    public interface LocalStorage {
        void putBankList(BankConfigResponse bankConfigResponse);

        long getExpireTime();

        Observable<BankConfigResponse> getBankList();

        String getCheckSum();

        String getMap();
    }

    public interface Repository {
        Observable<BankConfigResponse> fetchBankListCloud(String platform, String checksum, String appversion);

        BankListStore.LocalStorage getLocalStorage();
    }

    public interface BankListService {
        @GET(Constants.URL_GET_BANKLIST)
        @API_NAME(ZPEvents.CONNECTOR_V001_TPE_GETBANKLIST)
        Observable<BankConfigResponse> fetchBankList(@Query("platform") String platform, @Query("checksum") String checksum, @Query("appversion") String appversion);
    }
}
