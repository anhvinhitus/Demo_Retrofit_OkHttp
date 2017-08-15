package vn.com.zalopay.wallet.business.entity.voucher;

import com.google.gson.annotations.SerializedName;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

/*
 * Created by chucvv on 8/1/17.
 */

public class UseVoucherResponse extends BaseResponse {
    @SerializedName("data")
    public VoucherInfo data;
}
