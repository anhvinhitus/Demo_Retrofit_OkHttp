package vn.com.zalopay.wallet.business.entity.atm;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class BankResponse extends BaseResponse {
    @SerializedName("banklist")
    public List<BankConfig> banklist;

    @SerializedName("bankcardprefixmap")
    public Map<String, String> bankcardprefixmap;

    @SerializedName("checksum")
    public String checksum;

    @SerializedName("expiredtime")
    public long expiredtime;
}
