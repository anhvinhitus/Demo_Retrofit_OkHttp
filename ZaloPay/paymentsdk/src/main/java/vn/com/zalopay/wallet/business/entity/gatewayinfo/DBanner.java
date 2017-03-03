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

            StringBuilder builderMe = new StringBuilder();
            builderMe.append(appid);
            builderMe.append(bannertype);
            builderMe.append(platformcode);
            builderMe.append(function);
            builderMe.append(dscreentypecode);
            builderMe.append(logourl);
            builderMe.append(webviewurl);
            builderMe.append(orderindex);

            StringBuilder builderOther = new StringBuilder();
            builderOther.append(other.appid);
            builderOther.append(other.bannertype);
            builderOther.append(other.platformcode);
            builderOther.append(other.function);
            builderOther.append(other.dscreentypecode);
            builderOther.append(other.logourl);
            builderOther.append(other.webviewurl);
            builderOther.append(other.orderindex);

            sameSame = (builderMe.toString().equals(builderOther.toString()));
        }
        return sameSame;
    }
}
