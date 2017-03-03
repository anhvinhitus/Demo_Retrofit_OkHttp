package vn.com.zalopay.wallet.business.entity.base;

import android.text.TextUtils;

import com.google.gson.Gson;

import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.linkacc.LinkAccInfo;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;

public class ZPWPaymentInfo extends BaseEntity<ZPWPaymentInfo> {
    public long appID;
    public String appTransID;
    public String walletTransID;
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
    public LinkAccInfo linkAccInfo;

    public int[] forceChannelIds;

    public ZPWPaymentInfo() {
        mapBank = new DMappedCard();
    }

    public static ZPWPaymentInfo fromJson(String pJson) {
        return (new Gson()).fromJson(pJson, ZPWPaymentInfo.class);
    }

    public boolean isForceChannel() {
        return forceChannelIds != null && forceChannelIds.length >= 1;
    }

    public boolean isPaymentInfoValid() {
        return appID > 0 && userInfo != null && !TextUtils.isEmpty(userInfo.zaloPayUserId) && !TextUtils.isEmpty(userInfo.accessToken);
    }
}
