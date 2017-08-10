package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lytm on 10/08/2017.
 */

public class Withdraw {
    @SerializedName("withdraw_money")
    public List<Long> denominationWithdraw;

    @SerializedName("min_withdraw_money")
    public long minWithdrawMoney;

    @SerializedName("max_withdraw_money")
    public long maxWithdrawMoney;

    @SerializedName("multiple_withdraw_money")
    public long multipleWithdrawMoney;

}
