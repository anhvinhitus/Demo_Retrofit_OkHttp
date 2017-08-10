package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lytm on 10/08/2017.
 */

public class TabMe {
    @SerializedName("quick_comment_url")
    public String mFeedbackUrl;

    @SerializedName("enable_register_profile_zalopayid")
    public int mEnableRegisterZalopayID;
}
