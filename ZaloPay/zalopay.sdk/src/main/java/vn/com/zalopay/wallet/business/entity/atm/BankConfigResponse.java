package vn.com.zalopay.wallet.business.entity.atm;

import java.util.HashMap;
import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class BankConfigResponse extends BaseResponse {

    public List<BankConfig> banklist;

    public HashMap<String, String> bankcardprefixmap;

    public String checksum;

    public long expiredtime;
}
