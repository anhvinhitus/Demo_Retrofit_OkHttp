package vn.com.vng.zalopay.data.repository;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;
import vn.com.vng.zalopay.data.api.entity.mapper.AppConfigEntityDataMapper;
import vn.com.vng.zalopay.data.api.entity.mapper.ApplicationEntityDataMapper;
import vn.com.vng.zalopay.data.api.response.AppResourceResponse;
import vn.com.vng.zalopay.data.api.response.PlatformInfoResponse;
import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class AppConfigRepositoryImpl extends BaseRepository implements AppConfigRepository {

    private AppConfigFactory appConfigFactory;
    private AppConfigEntityDataMapper mapper;
    public AppConfigRepositoryImpl(AppConfigFactory factory, AppConfigEntityDataMapper mapper) {
        this.appConfigFactory = factory;
        this.mapper = mapper;
    }


    @Override
    public Observable<Boolean> initialize() {
        return makeObservable(() -> {
            appConfigFactory.getPlatformInfo()
                    .subscribe(new DefaultSubscriber<>())
            ;

            appConfigFactory.getAppResource()
                    .subscribe(new DefaultSubscriber<>());
            return Boolean.TRUE;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<BankCard>> listCard() {
        return appConfigFactory.listCard().map(cardEntities -> mapper.transform(cardEntities));
    }
}
