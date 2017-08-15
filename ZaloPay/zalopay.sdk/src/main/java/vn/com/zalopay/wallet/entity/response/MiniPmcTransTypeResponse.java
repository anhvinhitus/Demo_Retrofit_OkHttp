package vn.com.zalopay.wallet.entity.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.entity.gatewayinfo.MiniPmcTransType;

public class MiniPmcTransTypeResponse {

    @SerializedName("transtype")
    @TransactionType public int transtype;

    @SerializedName("transtypes")
    public List<MiniPmcTransType> transtypes;

    @SerializedName("checksum")
    public String checksum;
}
