package vn.com.vng.zalopay.data.notification;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.Field;
import retrofit2.http.Query;
import rx.Observable;
import vn.com.vng.zalopay.data.ApplicationTestCase;
import vn.com.vng.zalopay.data.BuildConfig;
import vn.com.vng.zalopay.data.CustomObserver;
import vn.com.vng.zalopay.data.CustomRobolectricRunner;
import vn.com.vng.zalopay.data.DefaultObserver;
import vn.com.vng.zalopay.data.api.entity.UserRPEntity;
import vn.com.vng.zalopay.data.api.entity.mapper.RedPacketDataMapper;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.BundleOrderResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.GetReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.PackageStatusResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.ReceivePackageResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.RedPacketAppInfoResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleListResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SentPackageInBundleResponse;
import vn.com.vng.zalopay.data.api.response.redpacket.SubmitOpenPackageResponse;
import vn.com.vng.zalopay.data.cache.model.DaoMaster;
import vn.com.vng.zalopay.data.cache.model.DaoSession;
import vn.com.vng.zalopay.data.cache.model.GetReceivePacket;
import vn.com.vng.zalopay.data.cache.model.PackageInBundleGD;
import vn.com.vng.zalopay.data.cache.model.ReceivePackageGD;
import vn.com.vng.zalopay.data.cache.model.SentBundleGD;
import vn.com.vng.zalopay.data.redpacket.RedPacketLocalStorage;
import vn.com.vng.zalopay.data.redpacket.RedPacketRepository;
import vn.com.vng.zalopay.data.redpacket.RedPacketStore;
import vn.com.vng.zalopay.data.rxbus.RxBus;
import vn.com.vng.zalopay.data.ws.model.NotificationData;
import vn.com.vng.zalopay.data.ws.model.NotificationEmbedData;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.redpacket.AppConfigEntity;
import vn.com.vng.zalopay.domain.model.redpacket.BundleOrder;
import vn.com.vng.zalopay.domain.model.redpacket.GetSentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageInBundle;
import vn.com.vng.zalopay.domain.model.redpacket.PackageStatus;
import vn.com.vng.zalopay.domain.model.redpacket.ReceivePackage;
import vn.com.vng.zalopay.domain.model.redpacket.RedPacketAppInfo;
import vn.com.vng.zalopay.domain.model.redpacket.SentBundle;
import vn.com.vng.zalopay.domain.model.redpacket.SubmitOpenPackage;

import static org.junit.Assert.assertEquals;

public class NotificationRepositoryTest extends ApplicationTestCase {
    NotificationStore.Repository mRepository;
    NotificationStore.LocalStorage mLocalStorage;
    NotificationStore.RequestService mRequestService;
    User mUser;

    BaseResponse baseResponse;

    private final int TRANSACTION_SIZE = 20;

    private List<NotificationData> entities = new ArrayList<>();


    public class RequestService implements NotificationStore.RequestService {
        @Override
        public Observable<BaseResponse> sendNotification(@Query("userid") String uid, @Query("accesstoken") String accesstoken, @Query("receiverid") String receiverid, @Query("embededdata") String embededdata) {
            return Observable.just(baseResponse);
        }
    }

    @Before
    public void setUp() throws Exception {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(RuntimeEnvironment.application, null, null);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        DaoSession daoSession = new DaoMaster(db).newSession();
        mLocalStorage = new NotificationLocalStorage(daoSession);
        mUser = new User("uid");
        mRepository = new NotificationRepository(mLocalStorage, EventBus.getDefault(), new RxBus(), mRequestService, mUser);
    }

    private void initData() {
        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            int j = i + 1;
            NotificationData entity = new NotificationData();

            entity.appid = j;
            entity.area = 124124L;
            entity.destuserid = "abc";
            entity.embeddata = new NotificationEmbedData(new JsonObject());
            entity.message = "message";
            entity.timestamp = 1241356L + j;
            entity.notificationId = j;
            entity.transid = j;
            entity.userid = "abc";
            entity.notificationstate = 1;
            entity.notificationtype = 1;
            entity.mtaid = 1;
            entity.mtuid = 0;

            entities.add(entity);
        }

        baseResponse = new BaseResponse();
        baseResponse.accesstoken = "";
        baseResponse.err = 1;
    }

    @Test
    public void testIt() {
        assertEquals(true, true);
    }
}
