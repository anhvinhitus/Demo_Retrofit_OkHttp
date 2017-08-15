package vn.com.vng.zalopay.data.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.util.ConvertHelper;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.domain.model.MoneyTransferModeEnum;

/**
 * Created by AnhHieu on 8/15/16.
 * Implementation for TransferStore.Repository
 */
public class TransferRepository implements TransferStore.Repository {

    TransferStore.LocalStorage mLocalStorage;

    public TransferRepository(TransferStore.LocalStorage mLocalStorage) {
        this.mLocalStorage = mLocalStorage;
    }

    @Override
    public Observable<List<RecentTransaction>> getRecent() {
        return ObservableHelper.makeObservable(() -> transform(mLocalStorage.get()));
    }

    @Override
    public Observable<Boolean> append(RecentTransaction item) {
        return ObservableHelper.makeObservable(() -> {
            TransferRecent transferRecent = new TransferRecent();
            transferRecent.zaloPayId = item.zaloPayId;
            transferRecent.zaloPayName = item.zaloPayName;
            transferRecent.displayName = item.displayName;
            transferRecent.avatar = item.avatar;
            transferRecent.phoneNumber = item.phoneNumber;
            transferRecent.transferType = (long)item.transferMode.getValue();
            transferRecent.amount = item.amount;
            transferRecent.message = item.message;
            transferRecent.timeCreate = System.currentTimeMillis();

            mLocalStorage.append(transferRecent);
            return Boolean.TRUE;
        });
    }

    public RecentTransaction transform(TransferRecent item) {
        if (item != null) {
            RecentTransaction transaction = new RecentTransaction();
            transaction.zaloId = 0L;
            transaction.zaloPayId = item.zaloPayId;
            transaction.zaloPayName = item.zaloPayName;
            transaction.displayName = item.displayName;
            transaction.avatar = item.avatar;
            transaction.phoneNumber = item.phoneNumber;
            transaction.amount = ConvertHelper.unboxValue(item.amount, 0L);
            transaction.message = item.message;
            transaction.transferMode = MoneyTransferModeEnum.fromInt((int)ConvertHelper.unboxValue(item.transferType, 0L));
            return transaction;
        }
        return null;
    }

    public List<RecentTransaction> transform(List<TransferRecent> entity) {

        if (Lists.isEmptyOrNull(entity)) {
            return Collections.emptyList();
        }
        List<RecentTransaction> items = new ArrayList<>();
        for (TransferRecent item : entity) {
            if (item != null) {
                items.add(transform(item));
            }
        }

        return items;
    }
}
