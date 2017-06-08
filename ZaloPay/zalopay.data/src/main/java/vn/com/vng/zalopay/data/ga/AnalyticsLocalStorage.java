package vn.com.vng.zalopay.data.ga;

import java.util.List;

import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.global.DaoSession;
import vn.com.vng.zalopay.data.cache.global.GoogleAnalytics;
import vn.com.vng.zalopay.data.cache.global.GoogleAnalyticsDao;
import vn.com.vng.zalopay.data.util.Lists;

/**
 * Created by hieuvm on 6/6/17.
 * *
 */

public class AnalyticsLocalStorage implements AnalyticsStore.LocalStorage {

    private final GoogleAnalyticsDao mGoogleAnalyticsDao;

    public AnalyticsLocalStorage(DaoSession daoSession) {
        mGoogleAnalyticsDao = daoSession.getGoogleAnalyticsDao();
    }

    @Override
    public void append(String type, String payload) {
       // Timber.d("append analytics [type: %s payload: %s]", type, payload);
        try {
            mGoogleAnalyticsDao.insertInTx(transform(type, payload));
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public List<String> getAll() {
        List<GoogleAnalytics> list = mGoogleAnalyticsDao.queryBuilder()
                .orderAsc(GoogleAnalyticsDao.Properties.Timestamp)
                .list();
        return transforms(list);
    }

    @Override
    public List<GoogleAnalytics> get(int limit) {
        return mGoogleAnalyticsDao.queryBuilder()
                .limit(limit)
                .orderAsc(GoogleAnalyticsDao.Properties.Timestamp)
                .list();
    }

    @Override
    public List<String> getPayload(int limit) {
        List<GoogleAnalytics> list = mGoogleAnalyticsDao.queryBuilder()
                .limit(limit)
                .orderAsc(GoogleAnalyticsDao.Properties.Timestamp)
                .list();
        return transforms(list);
    }

    @Override
    public void remove(long timestamp) {
        mGoogleAnalyticsDao.queryBuilder()
                .where(GoogleAnalyticsDao.Properties.Timestamp.le(timestamp))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    private List<String> transforms(List<GoogleAnalytics> list) {
        return Lists.transform(list, this::transform);
    }

    private String transform(GoogleAnalytics analytics) {
        return analytics.payload;
    }

    private GoogleAnalytics transform(String type, String payload) {
        GoogleAnalytics analytics = new GoogleAnalytics();
        analytics.type = type;
        analytics.payload = payload;
        analytics.timestamp = System.currentTimeMillis();
        return analytics;
    }

    @Override
    public long count() {
        return mGoogleAnalyticsDao.count();
    }
}
