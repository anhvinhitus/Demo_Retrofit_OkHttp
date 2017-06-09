package vn.com.zalopay.wallet.business.entity.gatewayinfo;

import java.util.List;

import vn.com.zalopay.wallet.constants.TransactionType;

public class MiniPmcTransTypeResponse {
    @TransactionType public int transtype;
    public List<MiniPmcTransType> transtypes;
    public String checksum;
}
