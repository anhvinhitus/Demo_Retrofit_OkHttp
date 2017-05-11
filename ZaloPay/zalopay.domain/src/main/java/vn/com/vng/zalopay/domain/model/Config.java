package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.List;

/**
 * Created by longlv on 2/13/17.
 * Parse file config in resource app 1.
 */

public class Config {

    @SerializedName("phone_format")
    public PhoneFormat mPhoneFormat;

    @SerializedName("inside_app")
    public List<InsideApp> mInsideAppList;

    @SerializedName("friend_config")
    public FriendConfig friendConfig;

    @SerializedName("api_route")
    public String apiRoute = "https"; //"https|connector"

    @SerializedName("api_names")
    public HashSet<String> apiNames;

    public static class FriendConfig {
        @SerializedName("enable_merge_contact_name")
        public int enableMergeContactName = 1;
    }

}
