package vn.com.vng.zalopay.domain.repository;

/**
 * Created by huuhoa on 5/29/16.
 * Interface for local resource service
 */
public interface LocalResourceRepository {
    String getInternalResourceVersion();
    String getExternalResourceVersion(int appId);
    void setInternalResourceVersion(String version);
    void setExternalResourceVersion(int appId, String version);
}
