package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by longlv on 2/13/17.
 * Phone format in resource app 1.
 */

public class PhoneFormat {

    @SerializedName("min_length")
    public int mMinLength;

    @SerializedName("max_length")
    public int mMaxLength;

    @SerializedName("patterns")
    public List<String> mPatterns;

    @SerializedName("invalid_length_message")
    public String mInvalidLengthMessage;

    @SerializedName("general_message")
    public String mGeneralMessage;

}
