package vn.com.vng.zalopay.authentication;

/**
 * Created by hieuvm on 1/4/17.
 */

interface AuthenticationProvider {

    void setStage(Stage stage);

    void verify(String password);

    void startVerify();

    void stopVerify();

    void setCallback(Callback callback);

    interface Callback {

        void onAuthenticated(String password);

        void onError(Throwable e);
    }
}
