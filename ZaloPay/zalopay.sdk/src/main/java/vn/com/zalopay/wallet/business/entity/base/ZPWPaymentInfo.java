package vn.com.zalopay.wallet.business.entity.base;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;

public class ZPWPaymentInfo {
    public long appID;
    public String appTransID;
    public String appUser;
    public long appTime;
    public long amount;
    public String itemName;
    public String description;
    public String embedData;
    public String mac;
    public String chargeInfo;
    public DBaseMap mapBank;
    public UserInfo userInfo;
    public UserInfo userTransfer;
    public LinkAccInfo linkAccInfo;
    public int[] forceChannelIds;
    public PaymentLocation mLocation = new PaymentLocation();

    public ZPWPaymentInfo() {
        mapBank = new DMappedCard();
    }

    public boolean isForceChannel() {
        return forceChannelIds != null && forceChannelIds.length >= 1;
    }

    public boolean isPaymentInfoValid() {
        return appID > 0 && userInfo != null && !TextUtils.isEmpty(userInfo.zaloPayUserId) && !TextUtils.isEmpty(userInfo.accessToken);
    }
}
