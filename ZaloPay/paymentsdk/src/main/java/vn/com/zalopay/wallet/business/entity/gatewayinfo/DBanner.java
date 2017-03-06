package vn.com.zalopay.wallet.business.entity.gatewayinfo;

public class DBanner {
    public int appid;
    public int bannertype;
    public String platformcode;
    public int function;
    public String dscreentypecode;
    public String logourl;
    public String webviewurl;
    public int orderindex;

    @Override
    public boolean equals(Object object) {
        boolean sameSame = false;
        if (object instanceof DBanner) {
            DBanner other = (DBanner) object;

            String builderMe = String.valueOf(appid) +
                    bannertype +
                    platformcode +
                    function +
                    dscreentypecode +
                    logourl +
                    webviewurl +
                    orderindex;

            String builderOther = String.valueOf(other.appid) +
                    other.bannertype +
                    other.platformcode +
                    other.function +
                    other.dscreentypecode +
                    other.logourl +
                    other.webviewurl +
                    other.orderindex;

            sameSame = (builderMe.equals(builderOther));
        }
        return sameSame;
    }
}
