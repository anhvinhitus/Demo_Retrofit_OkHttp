package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.entity.PaymentTransTypeEntity;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface SqlitePlatformScope extends SqlBaseScope {
    void writeCards(List<CardEntity> listCard);

    void write(CardEntity card);

    Observable<List<CardEntity>> listCard();

    void write(List<AppResourceEntity> listApp);

    Observable<List<AppResourceEntity>> listApp();

    List<AppResourceEntity> listAppResourceEntity();

    // Xoá app, nếu không tồn tại trong list này.
    void updateAppId(List<Long> list);

    void writePaymentTransType(List<PaymentTransTypeEntity> list);


}
