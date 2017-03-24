package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;

/**
 * Created by AnhHieu on 5/18/16.
 */
public class AppResourceResponse extends BaseResponse {

    @SerializedName("expiredtime")
    public long expiredtime;

    @SerializedName("baseurl")
    public String baseurl;

    @SerializedName("appidlist")
    public List<Long> appidlist;

    @SerializedName("orderedInsideApps")
    public List<Long> orderedInsideApps;

    @SerializedName("resourcelist")
    public List<AppResourceEntity> resourcelist;

}