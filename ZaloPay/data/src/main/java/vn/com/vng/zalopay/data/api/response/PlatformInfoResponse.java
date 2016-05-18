package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.entity.PCMEntity;

/**
 * Created by AnhHieu on 4/28/16.
 */
public class PlatformInfoResponse extends BaseResponse {


    @SerializedName("expiredtime")
    public long expiredtime; // thời gian expire của data trả về (miliseconds) , sau thời gian này client cần gọi lại api  để cập nhật data mới nhất


    @SerializedName("isupdateresource")
    public boolean isupdateresource;

    @SerializedName("platforminfochecksum")
    public String platforminfochecksum;

    @SerializedName("isupdateplatforminfo")
    public boolean isupdateplatforminfo;

    @SerializedName("platforminfo")
    public PlatformInfoEntity platforminfo;

    @SerializedName("resource")
    public ResourceEntity resource;

    @SerializedName("cardlist")
    public List<CardEntity> cardlist;

    public static class PlatformInfoEntity {
        public List<PCMEntity> pmclist;
    }

    public static class ResourceEntity {
        public String rsversion;
        public String rsurl;
    }
}
