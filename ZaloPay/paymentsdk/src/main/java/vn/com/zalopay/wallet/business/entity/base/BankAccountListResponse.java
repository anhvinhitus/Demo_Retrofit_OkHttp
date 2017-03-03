package vn.com.zalopay.wallet.business.entity.base;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;

public class BankAccountListResponse extends BaseResponse {
    public String bankaccountchecksum = null;
    public List<DBankAccount> bankaccounts = null;
}
