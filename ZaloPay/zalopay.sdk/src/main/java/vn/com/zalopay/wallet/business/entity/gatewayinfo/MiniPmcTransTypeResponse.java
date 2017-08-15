package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.zalopay.wallet.constants.TransactionType;

public class MiniPmcTransTypeResponse {

    @SerializedName("transtype")
    @TransactionType public int transtype;

    @SerializedName("transtypes")
    public List<MiniPmcTransType> transtypes;

    @SerializedName("checksum")
    public String checksum;
}
