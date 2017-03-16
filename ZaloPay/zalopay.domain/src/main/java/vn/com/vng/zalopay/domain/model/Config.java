package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

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
}
