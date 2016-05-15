package vn.com.vng.zalopay.mdl.internal;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

import rx.functions.Func1;
import rx.internal.operators.OperatorToMultimap;
import timber.log.Timber;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.TransHistory;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;

/**
 * Created by huuhoa on 5/8/16.
 */
public class ReactTransactionLogsNativeModule extends ReactContextBaseJavaModule {

    private ZaloPayRepository repository;

    public ReactTransactionLogsNativeModule(ReactApplicationContext reactContext, ZaloPayRepository repository) {
        super(reactContext);
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "ZaloPayTransactionLogs";
    }

    @ReactMethod
    public void getTransactions(int pageIndex, int count, Promise promise) {

        Timber.d("get transaction index %s count %s", pageIndex, count);

        repository.getTransactions(pageIndex, count)
                .map(new Func1<List<TransHistory>, WritableArray>() {
                    @Override
                    public WritableArray call(List<TransHistory> transHistories) {
                        return transform(transHistories);
                    }
                })
                .subscribe(new TransactionLogSubscriber(promise));
    }

    @ReactMethod
    public void reloadListTransaction(int count, Promise promise) {
        Timber.d("reload transaction count %s", count);
        repository.reloadListTransaction(count)
                .map(new Func1<List<TransHistory>, WritableArray>() {
                    @Override
                    public WritableArray call(List<TransHistory> transHistories) {
                        Timber.d("list transaction : %s", transHistories);

                        return transform(transHistories);
                    }
                })
                .subscribe(new TransactionLogSubscriber(promise));
    }

    private WritableArray transform(List<TransHistory> historys) {
        WritableArray result = Arguments.createArray();
        for (TransHistory history : historys) {
            WritableMap item = transform(history);
            if (item == null) continue;
            result.pushMap(item);
        }
        return result;
    }

    private WritableMap transform(TransHistory history) {
        if (history == null) return null;
        WritableMap item = Arguments.createMap();
        item.putDouble("transid", history.transid);
        item.putDouble("reqdate", history.reqdate);
        item.putString("description", history.description);
        item.putInt("amount", history.amount);
        item.putInt("type", history.type);
        return item;
    }


    private class TransactionLogSubscriber extends DefaultSubscriber<WritableArray> {

        private Promise promise;

        public TransactionLogSubscriber(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, " onError ");
        }

        @Override
        public void onNext(WritableArray writableArray) {

            Timber.d(" transaction log %s", writableArray);

            promise.resolve(writableArray);
        }
    }
}
