package vn.com.vng.zalopay.mdl;

/**
 * Created by huuhoa on 4/29/16.
 * Service interface for providing bundle resource settings
 */
public interface ResourceHandlerService {
    /**
     * Get folder path to the location where all bundles are stored
     * @return folder path to the location where all bundles are stored
     */
    String getBundleRoot();

    /**
     * Download a file from given [url], then save to [destinationPath]
     * @param url File URL
     * @param destinationPath Path to destination file
     */
    void downloadFile(String url, String destinationPath);

    /**
     * Update the latest miniapplication resource path. Call this method after updating new app version
     * @param appId application id, use 'internal' for internal RN app
     * @param path new resource path
     */
    void setLatestMiniApplicationPath(String appId, String path);

    /**
     * Unzip file to destination path
     * @param zipPathName Zip path name
     * @param destinationPath destination path that hold unzip content
     */
    void unzipFile(String zipPathName, String destinationPath);
}
