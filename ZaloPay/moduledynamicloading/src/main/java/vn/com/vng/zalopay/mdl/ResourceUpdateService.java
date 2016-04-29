package vn.com.vng.zalopay.mdl;

/**
 * Created by huuhoa on 4/29/16.
 * Service interface for providing bundle-related network request api
 */
public interface ResourceUpdateService {

    /**
     * Add new job for handling update resource of mini application appId
     * @param appId Id of pending-updated application
     */
    void addUpdateJob(String appId);

    /**
     * Background processing of resource update jobs in queue
     */
    void processQueue();

    /**
     * Remove done job
     * @param jobId job id
     */
    void removeJob(int jobId);
}
