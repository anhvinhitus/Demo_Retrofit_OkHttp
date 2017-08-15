package vn.com.zalopay.wallet.entity.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import vn.com.zalopay.wallet.entity.bank.BankAccount;

public class BankAccountResponse extends BaseResponse {
    @SerializedName("bankaccountchecksum")
    public String bankaccountchecksum = null;

    @SerializedName("bankaccounts")
    public List<BankAccount> bankaccounts = null;
}
