package vn.com.zalopay.wallet.repository.cardmap;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;

/**
 * Created by chucvv on 6/7/17.
 */

public class CardStore {
    public interface LocalStorage {
        void saveResponse(String pUserId, CardInfoListResponse cardInfoListResponse);

        void put(String pUserId, String checkSum, List<MapCard> cardList);

        String getCheckSum();

        String getCardKeyList(String userid);

        void setCard(String userid, BaseMap card);

        MapCard getCard(String userid, String cardKey);

        void setCardKeyList(String userid, String cardKeyList);

        void clearCheckSum();
    }

    public interface Repository {
        Observable<CardInfoListResponse> fetchCloud(String userid, String accesstoken, String checksum, String appversion);

        CardStore.LocalStorage getLocalStorage();
    }

    public interface CardMapService {
        @GET(Constants.URL_LISTCARDINFO)
        @API_NAME(value = {ZPEvents.API_UM_LISTCARDINFOFORCLIENT, ZPEvents.CONNECTOR_UM_LISTCARDINFOFORCLIENT})
        Observable<CardInfoListResponse> fetch(@Query("userid") String userid, @Query("accesstoken") String accesstoken,
                                               @Query("cardinfochecksum") String cardinfochecksum, @Query("appversion") String appversion);
    }
}
