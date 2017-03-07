package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class BalanceResponse extends BaseResponse {

    @SerializedName("userid")
    public long userid;

    @SerializedName("zpwbalance")
    public long zpwbalance;
}
