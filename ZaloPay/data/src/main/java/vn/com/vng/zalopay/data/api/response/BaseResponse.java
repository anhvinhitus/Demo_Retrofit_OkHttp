package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 3/24/16.
 */
public class BaseResponse {

    @SerializedName("returncode")
    public int err;

    @SerializedName("returnmessage")
    public String message;


    public boolean isSuccessfulResponse() {
        return err == 1;
    }
}
