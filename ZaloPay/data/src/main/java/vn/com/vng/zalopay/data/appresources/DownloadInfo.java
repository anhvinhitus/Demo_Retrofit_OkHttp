package vn.com.vng.zalopay.data.appresources;

/**
 * Created by AnhHieu on 5/23/16.
 */
public class DownloadInfo {

    public final String url;
    public final String appname;
    public final int appid;
    public final String checksum;

    public DownloadInfo(String url, String appname, int appid, String checksum) {
        this.url = url;
        this.appname = appname;
        this.appid = appid;
        this.checksum = checksum;
    }
}
