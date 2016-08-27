package vn.com.vng.zalopay.data.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.model.RecentTransaction;

/**
 * Created by AnhHieu on 8/15/16.
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
    public Observable<Boolean> append(RecentTransaction item, int transactionType) {
        TransferRecent transferRecent = new TransferRecent(
                Long.valueOf(item.getZaloPayId()),
                item.getZaloPayId(),
                item.getZaloPayName(),
                item.getDisplayName(),
                item.getAvatar(),
                0,
                "",
                true,
                item.getPhoneNumber(),
                transactionType,
                item.amount,
                item.message,
                System.currentTimeMillis());

        return ObservableHelper.makeObservable(() -> {
            mLocalStorage.append(transferRecent);
            return Boolean.TRUE;
        });
    }

    public RecentTransaction transform(TransferRecent item) {
        if (item != null) {
            return new RecentTransaction(
                    item.getId(),
                    item.getZaloPayId(),
                    item.getUserName(),
                    item.getDisplayName(),
                    item.getAvatar(),
                    item.getUserGender(),
                    item.getBirthday(),
                    item.getUsingApp(),
                    item.getPhoneNumber(),
                    item.getTransferType(),
                    item.getAmount(),
                    item.getMessage());
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
