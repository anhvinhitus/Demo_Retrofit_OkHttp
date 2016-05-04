package vn.com.vng.zalopay.domain.repository;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.AppInfo;

/**
 * Created by AnhHieu on 5/3/16.
 */
public interface ApplicationRepository {
    Observable<List<AppInfo>> getApplicationInfos();
}
