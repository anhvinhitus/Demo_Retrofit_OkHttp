package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by AnhHieu on 3/24/16.
 */
public class BaseResponse<T> {
   /* {"err": 0, message: “No error", data: null}*/

    @SerializedName("returncode")
    public int err;

    @SerializedName("returnmessage")
    public String message;

    @SerializedName("data")
    public T data;

    public boolean isSuccessfulResponse() {
        return err == 0;
    }

    public String message() {
        return message;
    }

    public T getData() {
        return data;
    }
}
