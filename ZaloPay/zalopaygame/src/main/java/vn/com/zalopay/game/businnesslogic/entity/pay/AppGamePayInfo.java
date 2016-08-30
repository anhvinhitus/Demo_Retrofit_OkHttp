package vn.com.zalopay.game.businnesslogic.entity.pay;

public class AppGamePayInfo
{
    private String uid;
    private String accessToken;
    private int appId;
    private String apptransid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getApptransid() {
        return apptransid;
    }

    public void setApptransid(String apptransid) {
        this.apptransid = apptransid;
    }
}
