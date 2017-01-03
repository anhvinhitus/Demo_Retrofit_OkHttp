package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.ZaloPayUserEntity;

/**
 * Created by hieuvm on 11/25/16.
 */

public class ListUserExistResponse extends BaseResponse {
    @SerializedName("userslist")
    public List<ZaloPayUserEntity> userList;
}
