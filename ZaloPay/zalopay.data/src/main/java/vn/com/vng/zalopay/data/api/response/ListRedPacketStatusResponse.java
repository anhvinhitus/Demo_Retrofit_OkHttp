package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.RedPacketStatusEntity;

/**
 * Created by hieuvm on 1/4/17.
 */

public class ListRedPacketStatusResponse extends BaseResponse {

    @SerializedName("listpackagestatus")
    public List<RedPacketStatusEntity> listpackagestatus;
}
