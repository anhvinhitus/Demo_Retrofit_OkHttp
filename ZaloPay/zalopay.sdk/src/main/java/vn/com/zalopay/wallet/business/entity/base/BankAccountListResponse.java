package vn.com.zalopay.wallet.business.entity.base;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;

public class BankAccountListResponse extends BaseResponse {
    public String bankaccountchecksum = null;
    public List<BankAccount> bankaccounts = null;
    public BankAccountListResponse(){}
    public BankAccountListResponse(String checksum, List<BankAccount> bankAccounts) {
        this.bankaccountchecksum = checksum;
        this.bankaccounts = bankAccounts;
    }
}
