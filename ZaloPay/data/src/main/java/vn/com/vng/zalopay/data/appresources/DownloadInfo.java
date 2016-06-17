package vn.com.vng.zalopay.data.appresources;

/**
 * Created by AnhHieu on 5/23/16.
 */
public class DownloadInfo {

    public String url;

    public String appname;

    public int appid;

    public String checksum;

    public DownloadInfo(String url, String appname, int appid, String checksum) {
        this.url = url;
        this.appname = appname;
        this.appid = appid;
        this.checksum = checksum;
    }
}
