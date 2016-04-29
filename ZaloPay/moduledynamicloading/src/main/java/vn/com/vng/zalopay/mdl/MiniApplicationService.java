package vn.com.vng.zalopay.mdl;

/**
 * Created by huuhoa on 4/29/16.
 * Interface for service that manage all mini-applications
 */
public interface MiniApplicationService {
    /**
     * Run in background thread.
     * Check for updated application list from server. When there are some updates,
     * it will create jobs that will execute those updates
     */
    void checkForUpdate();

    /**
     * Get local file url of the mini-application given the appId
     * @param appId Id of the mini-application. 'internal' for internal RN functionalities
     * @return local file url of the mini-application
     */
    String getAppLocalUrl(String appId);
}
