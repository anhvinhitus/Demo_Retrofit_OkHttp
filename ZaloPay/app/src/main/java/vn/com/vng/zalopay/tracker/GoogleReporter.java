package vn.com.vng.zalopay.tracker;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Retrofit;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by hieuvm on 6/2/17.
 * *
 */

public class GoogleReporter {

    public static final String BASE_URL = "https://www.google-analytics.com/collect/";
    public static final String BASE_URL_2 = "https://www.google-analytics.com/batch/"; //multiple hits in a single request - https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide

    interface GoogleAnalyticsService {
        @POST
        @FormUrlEncoded
        @Headers({"User-Agent: ZaloPayClient/2.12"})
        Observable<String> send(@Url String url, @FieldMap Map<String, String> query);
    }

    private final GoogleAnalyticsService mAnalyticsService;

    @Inject
    public GoogleReporter(@Named("retrofitGoogleAnalytics") Retrofit retrofit) {
        mAnalyticsService = retrofit.create(GoogleAnalyticsService.class);
    }

    private void send(Map<String, String> values) {
        mAnalyticsService.send(BASE_URL, values)
                .subscribe(new DefaultSubscriber<>());
    }

    void trackScreen(String screenName) {
        Map<String, String> params = buildParams();
        params.put("t", "screenview"); //Required
        params.put("cd", screenName);
        send(params);
    }

    void trackEvent(int eventId, Long eventValue) {
        Map<String, String> params = buildParams();
        params.put("t", "event"); //Required
        params.put("ec", ZPEvents.categoryFromEventId(eventId));
        params.put("ea", ZPEvents.actionFromEventId(eventId));
        params.put("el", ZPEvents.actionFromEventId(eventId));

        if (eventValue != null) {
            params.put("ev", String.valueOf(eventValue));
        }

        send(params);
    }

    void trackTiming(int eventId, long value) {
        Map<String, String> params = buildParams();
        params.put("t", "timing"); //Required
        params.put("utc", ZPEvents.categoryFromEventId(eventId));
        params.put("utv", ZPEvents.actionFromEventId(eventId));
        params.put("utl", ZPEvents.actionFromEventId(eventId));
        params.put("utt", String.valueOf(value));
        send(params);
    }

    private Map<String, String> buildParams() {
        Map<String, String> params = new HashMap<>();
        params.put("tid", BuildConfig.GA_Tracker); //Required
        params.put("v", "1"); //Required
        params.put("aid", BuildConfig.APPLICATION_ID);
        params.put("cid", AndroidUtils.getDeviceId());
        params.put("an", "Zalo Pay");
        params.put("av", BuildConfig.VERSION_NAME);
        params.put("ua", Constants.UserAgent.ZALO_PAY_CLIENT + BuildConfig.VERSION_NAME);
        params.put("ul", Locale.getDefault().getDisplayName());
        params.put("sr", String.valueOf(AndroidUtils.density));
        return params;
    }

}
