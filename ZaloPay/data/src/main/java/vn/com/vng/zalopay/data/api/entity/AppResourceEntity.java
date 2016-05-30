package vn.com.vng.zalopay.data.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/18/16.
 */
public class AppResourceEntity {

    @SerializedName("appid")
    public int appid;

    @SerializedName("appname")
    public String appname;

    @SerializedName("needdownloadrs")
    public int needdownloadrs;

    @SerializedName("imageurl")
    public String imageurl;

    @SerializedName("jsurl")
    public String jsurl;

    @SerializedName("status")
    public int status;

    @SerializedName("checksum")
    public String checksum;

}
