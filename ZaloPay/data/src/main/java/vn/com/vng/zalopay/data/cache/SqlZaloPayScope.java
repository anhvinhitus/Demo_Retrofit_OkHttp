package vn.com.vng.zalopay.data.cache;

import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.cache.model.ZaloFriend;

/**
 * Created by AnhHieu on 5/4/16.
 */
public interface SqlZaloPayScope extends SqlBaseScope {

    void write(List<TransHistoryEntity> val);

    void write(TransHistoryEntity val);

    Observable<List<TransHistoryEntity>> transactionHistorys();

    Observable<List<TransHistoryEntity>> transactionHistorys(int limit);

    List<TransHistoryEntity> listTransHistorys(int limit);

    boolean isHaveTransactionInDb();

    Observable<TransHistoryEntity> transactionHistory();

    Observable<Long> balance();

    void writeBalance(long balance);

    void writeZaloFriends(List<ZaloFriend> val);

    void writeZaloFriend(ZaloFriend val);

    List<ZaloFriend> listZaloFriend();

    List<ZaloFriend> listZaloFriend(int limit);

    boolean isHaveZaloFriendDb();

    void writeTransferRecent(TransferRecent val);

    List<TransferRecent> listTransferRecent();

    List<TransferRecent> listTransferRecent(int limit);
}
