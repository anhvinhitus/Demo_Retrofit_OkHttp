package vn.com.vng.zalopay.mdl;

import java.util.List;

/**
 * Created by huuhoa on 4/29/16.
 * Interface for mini application storage service
 */
public interface MiniApplicationStorageService {
    /**
     * Get list of mini application with summary info (id, checksum)
     * @return list of mini applications
     */
    List<MiniAppInfoSummary> getAppListSummary();

    /**
     * Get list of mini applications with full information
     * @return list of mini applications
     */
    List<MiniAppInfoSummary> getAppListFull();

    /**
     * Update list of mini applications.
     * Old apps which are not part of new list will be marked as removed
     * @param newApps List of new apps
     */
    void updateAppList(List<MiniAppInfoSummary> newApps);

    /**
     * Add new apps that will be updated
     * @param pendingApps List of pending information
     */
    void addPendingApp(List<MiniAppInfoFull> pendingApps);

    /**
     * Mark a pending update mini application as latest
     * @param appId Mini application Id
     */
    void updatePendingApp(String appId);

    /**
     * Get local file url of the mini-application given the appId
     * @param appId Id of the mini-application. 'internal' for internal RN functionalities
     * @return local file url of the mini-application
     */
    String getAppLocalUrl(String appId);
}
