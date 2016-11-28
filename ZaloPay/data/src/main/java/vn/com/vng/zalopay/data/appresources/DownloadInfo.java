package vn.com.vng.zalopay.data.appresources;

/**
 * Created by AnhHieu on 5/23/16.
 */
final class DownloadInfo {

    public final String url;
    public final String appname;
    public final int appid;
    public final String checksum;

     DownloadInfo(String url, String appname, int appid, String checksum) {
        this.url = url;
        this.appname = appname;
        this.appid = appid;
        this.checksum = checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadInfo that = (DownloadInfo) o;

        if (appid != that.appid) {
            return false;
        }

        return url != null ? url.equals(that.url) : that.url == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + appid;
        return result;
    }
}
