package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lytm on 10/08/2017.
 */

public class Zpc {
    @SerializedName("enable_merge_contact_name")
    public int enableMergeContactName = 1;
    @SerializedName("friend_favorite")
    public int enableDisplayFavorite = 1;
}
