package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lytm on 10/08/2017.
 */

public class General {
    @SerializedName("phone_format")
    public PhoneFormat mPhoneFormat;
    @SerializedName("max_cc_links")
    public int max_cc_links = 3;
}
