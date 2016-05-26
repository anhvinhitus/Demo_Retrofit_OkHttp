package vn.com.vng.zalopay.domain.repository;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.BankCard;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface AppConfigRepository {
    Observable<Boolean> initialize();

    Observable<List<BankCard>> listCardCache();


    Observable<List<AppResource>> listAppResource();
}
