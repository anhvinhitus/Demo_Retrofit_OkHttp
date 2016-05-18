package vn.com.vng.zalopay.data.repository;

import rx.Observable;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class AppConfigRepositoryImpl extends BaseRepository implements AppConfigRepository {

    private AppConfigFactory appConfigFactory;

    public AppConfigRepositoryImpl(AppConfigFactory factory) {
        this.appConfigFactory = factory;
    }


    @Override
    public Observable<Boolean> initialize() {
        return makeObservable(() -> {
            appConfigFactory.getPlatformInfo()
                    .subscribe(new DefaultSubscriber<>())
            ;
            return Boolean.TRUE;
        }).subscribeOn(Schedulers.io());
    }
}
