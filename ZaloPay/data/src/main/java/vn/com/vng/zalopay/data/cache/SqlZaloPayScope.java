package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;

/**
 * Created by AnhHieu on 5/4/16.
 */
public interface SqlZaloPayScope {

    void write(List<TransHistoryEntity> val);

    void write(TransHistoryEntity val);

    Observable<List<TransHistoryEntity>> transactionHistorys();

    Observable<TransHistoryEntity> transactionHistory();

    Observable<Long> balance();

    void writeBalance(long balance);
}
