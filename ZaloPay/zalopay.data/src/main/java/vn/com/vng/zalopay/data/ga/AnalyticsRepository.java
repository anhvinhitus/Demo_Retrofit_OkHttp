package vn.com.vng.zalopay.data.ga;

import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.GoogleAnalytics;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by hieuvm on 6/6/17.
 * *
 */

public class AnalyticsRepository implements AnalyticsStore.Repository {

    private static final int MAX_LENGTH_PAYLOAD = 60;

    private final AnalyticsStore.RequestService mRequestService;
    private final AnalyticsStore.LocalStorage mLocalStorage;

    private boolean isSending; // TODO: 6/7/17
    private final String mUserAgent;

    public AnalyticsRepository(AnalyticsStore.LocalStorage localStorage, AnalyticsStore.RequestService requestService, String userAgent) {
        mRequestService = requestService;
        mLocalStorage = localStorage;
        mUserAgent = userAgent;
    }

    @Override
    public Observable<Boolean> append(String type, String payload) {
        return ObservableHelper
                .makeObservable(() -> {
                    mLocalStorage.append(type, payload);
                    return Boolean.TRUE;
                })
                .filter(aBoolean -> mLocalStorage.count() >= MAX_LENGTH_PAYLOAD)
                .flatMap(aBoolean -> sendBatch())
                ;
    }

    @Override
    public Observable<Boolean> sendBatch() {
        if (isSending) {
            return Observable.empty();
        }

        isSending = true;

        return ObservableHelper
                .makeObservable(this::getPayloadData)
                .filter(pair -> !TextUtils.isEmpty(pair.second))
                .flatMap(pair -> mRequestService.sendBatch(createRequestBody(pair.second), mUserAgent).map(s -> pair))
                .doOnNext(pair -> Timber.d("Send batch success"))
                .doOnNext(pair -> mLocalStorage.remove(pair.first))
                .map(s -> Boolean.TRUE)
                .doOnTerminate(() -> isSending = false)
                ;
    }

    private Pair<Long, String> getPayloadData() {
        List<GoogleAnalytics> analytics = mLocalStorage.get(20);
        Timber.d("get analytics [size %s]", analytics.size());

        if (Lists.isEmptyOrNull(analytics)) {
            return new Pair<>(0L, "");
        }

        List<String> list = new ArrayList<>();

        for (GoogleAnalytics analytic : analytics) {
            list.add(analytic.payload);
            // Timber.d("timestamp %s payload %s", analytic.timestamp, analytic.payload);
        }

        GoogleAnalytics endPayload = analytics.get(analytics.size() - 1);
        return new Pair<>(endPayload.timestamp, Strings.joinWithDelimiter("\n", list));
    }

    private RequestBody createRequestBody(String text) {
        return RequestBody.create(MediaType.parse("text/plain"), text);
    }
}
