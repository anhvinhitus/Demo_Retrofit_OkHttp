package vn.com.vng.zalopay.data.cache.model;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "APP_INFO".
 */
public class AppInfo {

    /** Not-null value. */
    private String app_id;
    private String app_name;
    private String app_icon_url;
    private String js_url;
    private String resource_url;
    private String base_url;
    private String app_checksum;
    private Integer status;
    private String app_local_url;

    public AppInfo() {
    }

    public AppInfo(String app_id) {
        this.app_id = app_id;
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

    /** Not-null value. */
    public String getApp_id() {
        return app_id;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getApp_icon_url() {
        return app_icon_url;
    }

    public void setApp_icon_url(String app_icon_url) {
        this.app_icon_url = app_icon_url;
    }

    public String getJs_url() {
        return js_url;
    }

    public void setJs_url(String js_url) {
        this.js_url = js_url;
    }

    public String getResource_url() {
        return resource_url;
    }

    public void setResource_url(String resource_url) {
        this.resource_url = resource_url;
    }

    public String getBase_url() {
        return base_url;
    }

    public void setBase_url(String base_url) {
        this.base_url = base_url;
    }

    public String getApp_checksum() {
        return app_checksum;
    }

    public void setApp_checksum(String app_checksum) {
        this.app_checksum = app_checksum;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getApp_local_url() {
        return app_local_url;
    }

    public void setApp_local_url(String app_local_url) {
        this.app_local_url = app_local_url;
    }

}
