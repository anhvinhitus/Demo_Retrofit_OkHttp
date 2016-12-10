package com.zalopay.apploader.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by AnhHieu on 5/26/16.
 */
public class ReactBundleAssetData {

    @SerializedName("external_bundle")
    public List<ExternalBundle> external_bundle;

    @SerializedName("internal_bundle")
    public InternalBundle internal_bundle;

    public static class ExternalBundle {
        @SerializedName("appid")
        public long appid;

        @SerializedName("appname")
        public String appname;

        @SerializedName("asset")
        public String asset;

        @SerializedName("checksum")
        public String checksum;

        @SerializedName("version")
        public String version;

        @SerializedName("status")
        public String status;
    }

    public static class InternalBundle {

        @SerializedName("checksum")
        public String checksum;

        @SerializedName("version")
        public String version;
    }
}
