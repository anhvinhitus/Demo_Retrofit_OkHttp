package vn.com.zalopay.wallet.business.entity.atm;

import java.util.List;
import java.util.Map;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

public class BankConfigResponse extends BaseResponse {

    public List<BankConfig> banklist;

    public Map<String, String> bankcardprefixmap;

    public String checksum;

    public long expiredtime;
}
