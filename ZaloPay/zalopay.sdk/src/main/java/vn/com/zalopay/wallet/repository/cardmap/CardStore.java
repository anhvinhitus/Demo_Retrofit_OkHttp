package vn.com.zalopay.wallet.repository.cardmap;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MapCard;
import vn.com.zalopay.wallet.constants.Constants;

/**
 * Created by chucvv on 6/7/17.
 */

public class CardStore {
    public interface LocalStorage {
        void saveResponse(String pUserId, CardInfoListResponse cardInfoListResponse);

        void put(String pUserId, String checkSum, List<MapCard> cardList);

        String getCheckSum();

        String getCardKeyList(String userid);

        void resetMapCardCache(String userId, String first6cardno, String last4cardno);

        void resetMapCardCacheList(String userId);

        void setCard(String userid, BaseMap card);

        MapCard getCard(String userid, String cardKey);

        void setCardKeyList(String userid, String cardKeyList);

        void clearCheckSum();
    }

    public interface CardMapService {
        @GET(Constants.URL_LISTCARDINFO)
        @API_NAME(https = ZPEvents.API_UM_LISTCARDINFOFORCLIENT, connector = ZPEvents.CONNECTOR_UM_LISTCARDINFOFORCLIENT)
        Observable<CardInfoListResponse> fetch(@Query("userid") String userid, @Query("accesstoken") String accesstoken,
                                               @Query("cardinfochecksum") String cardinfochecksum, @Query("appversion") String appversion);

        @POST(Constants.URL_REMOVE_MAPCARD)
        @API_NAME(https = ZPEvents.API_V001_TPE_REMOVEMAPCARD, connector = ZPEvents.CONNECTOR_V001_TPE_REMOVEMAPCARD)
        Observable<BaseResponse> removeMapCard(@Query("userid") String userid,
                                               @Query("accesstoken") String accessToken,
                                               @Query("cardname") String cardName,
                                               @Query("first6cardno") String first6Cardno,
                                               @Query("last4cardno") String last4Cardno,
                                               @Query("bankcode") String bankCode,
                                               @Query("appversion") String appver);
    }
}
