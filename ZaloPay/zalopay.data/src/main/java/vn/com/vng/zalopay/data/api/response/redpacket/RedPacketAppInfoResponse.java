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
        public long minAmountEach;

        @SerializedName("maxtotalamountperbundle")
        public long maxTotalAmountPerBundle;

        @SerializedName("maxpackagequantity")
        public long maxPackageQuantity;

        @SerializedName("maxcounthist")
        public long maxCountHist;

        @SerializedName("maxmessagelength")
        public long maxMessageLength;

        @SerializedName("bundleexpiredtime")
        public long bundleExpiredTime;

        @SerializedName("mindivideamount")
        public long minDivideAmount;

        @SerializedName("maxamountperpackage")
        public long maxAmountPerPackage;
    }
}
