package vn.com.zalopay.wallet.business.entity.staticconfig;

import java.util.HashMap;
import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.BaseEntity;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankScript;
import vn.com.zalopay.wallet.business.entity.staticconfig.atm.DOtpReceiverPattern;

public class DConfigFromServer extends BaseEntity<DConfigFromServer> {
    public HashMap<String, String> stringMap;
    public List<DPage> pageList;
    public HashMap<String, HashMap<String, String>> pattern;
    public List<DCardIdentifier> CCIdentifier;
    public List<DCardIdentifier> BankIdentifier;
    public List<DBankScript> bankScripts;
    public List<DOtpReceiverPattern> otpReceiverPattern;
    public List<DKeyBoardConfig> keyboard;
}
