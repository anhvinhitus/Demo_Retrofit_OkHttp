package vn.com.vng.zalopay.domain.repository;

import rx.Observable;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface AppConfigRepository {
    Observable<Boolean> getPlatformInfo();
}
