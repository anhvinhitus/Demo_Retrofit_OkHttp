package vn.com.vng.zalopay.data.repository;

import vn.com.vng.zalopay.data.repository.datasource.AppConfigFactory;
import vn.com.vng.zalopay.domain.repository.AppConfigRepository;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class AppConfigRepositoryImpl implements AppConfigRepository {

    private AppConfigFactory appConfigFactory;

    public AppConfigRepositoryImpl(AppConfigFactory factory) {
        this.appConfigFactory = factory;
    }

    @Override
    public void getPlatformInfo() {

    }
}
