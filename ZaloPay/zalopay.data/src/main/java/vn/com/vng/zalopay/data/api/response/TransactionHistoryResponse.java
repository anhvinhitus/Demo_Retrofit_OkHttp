package vn.com.vng.zalopay.data.api.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;

/**
 * Created by AnhHieu on 5/4/16.
 */
public class TransactionHistoryResponse extends BaseResponse {

    @SerializedName("data")
    public List<TransHistoryEntity> data;

}
