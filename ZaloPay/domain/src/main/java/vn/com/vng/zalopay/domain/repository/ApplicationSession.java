package vn.com.vng.zalopay.domain.repository;

import rx.Observable;

/**
 * Created by AnhHieu on 7/13/16.
 */
public interface ApplicationSession {
    void clearUserSession();

    void clearUserSessionWithoutSignOut();

    Observable<Boolean> clearMerchant();

    void clearAllUserDB();

    void newUserSession();

    void setMessageAtLogin(String message);

    void setMessageAtLogin(int message);

    void cancelAllRequest();
}
