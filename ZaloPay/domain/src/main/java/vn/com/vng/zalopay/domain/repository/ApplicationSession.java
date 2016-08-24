package vn.com.vng.zalopay.domain.repository;

/**
 * Created by AnhHieu on 7/13/16.
 */
public interface ApplicationSession {
    void clearUserSession();

    void clearAllUserDB();

    void newUserSession();

    void setMessageAtLogin(String message);
    void setMessageAtLogin(int message);
}
