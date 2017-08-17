package vn.com.zalopay.wallet.entity.voucher;

import com.google.gson.annotations.SerializedName;

import vn.com.zalopay.wallet.entity.response.BaseResponse;

/*
 * Created by chucvv on 8/1/17.
 */

public class VoucherStatusResponse extends BaseResponse {
    @SerializedName("voucherstatus")
    public int voucherstatus;
}
