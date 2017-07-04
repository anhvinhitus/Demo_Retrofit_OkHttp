package vn.com.zalopay.wallet.interactor;

/**
 * Created by chucvv on 6/8/17.
 */

public class VersionCallback extends PlatformInfoCallback {
    public boolean forceupdate;
    public String newestappversion;
    public String forceupdatemessage;

    public VersionCallback(boolean forceupdate, String newestappversion, String forceupdatemessage) {
        this.forceupdate = forceupdate;
        this.newestappversion = newestappversion;
        this.forceupdatemessage = forceupdatemessage;
    }
}
