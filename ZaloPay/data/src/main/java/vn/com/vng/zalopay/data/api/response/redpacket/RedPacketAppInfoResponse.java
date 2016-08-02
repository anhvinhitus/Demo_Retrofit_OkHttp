package vn.com.vng.zalopay.data.api.response.redpacket;

import com.google.gson.annotations.SerializedName;

/**
 * Created by longlv on 01/08/2016.
 * Data of getappinfo request
 */
public class RedPacketAppInfoResponse {

    @SerializedName("isupdateappinfo")
    public boolean isUpdateAppInfo;

    @SerializedName("checksum")
    public String checksum;

    @SerializedName("expiredtime")
    public long expiredTime;

    @SerializedName("info")
    public AppConfigResponse appConfigResponse;

    public class AppConfigResponse {
        @SerializedName("minamounteach")
        public long minAmounTeach;

        @SerializedName("maxtotalamountperbundle")
        public long maxTotalAmountPerBundle;

        @SerializedName("maxpackagequantity")
        public int maxPackageQuantity;

        @SerializedName("maxcounthist")
        public int maxCountHist;

        @SerializedName("maxmessagelength")
        public int maxMessageLength;

        @SerializedName("bundleExpiredTime")
        public long bundleExpiredTime;
    }
}
