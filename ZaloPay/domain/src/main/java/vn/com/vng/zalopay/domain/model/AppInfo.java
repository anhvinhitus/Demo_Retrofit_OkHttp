package vn.com.vng.zalopay.domain.model;

/**
 * Created by AnhHieu on 5/3/16.
 */
public class AppInfo {

    public String app_id;
    public String app_name;
    public String app_icon_url;
    public String js_url;
    public String resource_url;
    public String base_url;
    public String app_checksum;
    public Integer status;
    public String app_local_url;

    public AppInfo() {
    }

    public AppInfo(String app_id, String app_name, String app_icon_url, String js_url, String resource_url, String base_url, String app_checksum, Integer status, String app_local_url) {
        this.app_id = app_id;
        this.app_name = app_name;
        this.app_icon_url = app_icon_url;
        this.js_url = js_url;
        this.resource_url = resource_url;
        this.base_url = base_url;
        this.app_checksum = app_checksum;
        this.status = status;
        this.app_local_url = app_local_url;
    }

}
