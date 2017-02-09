package vn.com.vng.zalopay.utils;

import java.util.List;

import vn.com.zalopay.wallet.business.entity.base.ZPWRemoveMapCardParams;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBanner;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.WDMaintenance;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;
import vn.com.zalopay.wallet.merchant.listener.IGetWithDrawBankList;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;

/**
 * Created by longlv on 2/8/17.
 * Wrapper CShareData of PaymentSDK.
 */

public class CShareDataWrapper {

    public static void getCardSupportList(UserInfo userInfo,
                                          IGetCardSupportListListener listener) {
        CShareData.getInstance().setUserInfo(userInfo).getCardSupportList(listener);
    }

    public static List<DMappedCard> getMappedCardList(String zaloPayId) {
        return CShareData.getInstance().getMappedCardList(zaloPayId);
    }

    public static List<DBankAccount> getMapBankAccountList(String zaloPayId) {
        return CShareData.getInstance().getMapBankAccountList(zaloPayId);
    }

    public static ECardType detectCardType(UserInfo userInfo, String first6CardNo) {
        return CShareData.getInstance().setUserInfo(userInfo).detectCardType(first6CardNo);
    }

    public static void getWithDrawBankList(IGetWithDrawBankList listener) {
        CShareData.getInstance().getWithDrawBankList(listener);
    }

    public static WDMaintenance getWithdrawMaintenance() {
        return CShareData.getInstance().getWithdrawMaintenance();
    }

    public static long getPlatformInfoExpiredTime() {
        return CShareData.getInstance().getPlatformInfoExpiredTime();
    }

    public static boolean isEnableDeposite() {
        return CShareData.getInstance().isEnableDeposite();
    }

    public static long getMinTranferValue() {
        return CShareData.getInstance().getMinTranferValue();
    }

    public static long getMaxTranferValue() {
        return CShareData.getInstance().getMaxTranferValue();
    }

    public static long getMinDepositValue() {
        return CShareData.getInstance().getMinDepositValue();
    }

    public static long getMaxDepositValue() {
        return CShareData.getInstance().getMaxDepositValue();
    }

    public static long getMinWithDrawValue() {
        return CShareData.getInstance().getMinWithDrawValue();
    }

    public static long getMaxWithDrawValue() {
        return CShareData.getInstance().getMaxWithDrawValue();
    }

    public static void reloadMapCardList(ZPWRemoveMapCardParams params, IReloadMapInfoListener listener) {
        CShareData.getInstance().reloadMapCardList(params, listener);
    }

    public static List<DBanner> getBannerList() {
        return CShareData.getInstance().getBannerList();
    }

    public static void dispose() {
        CShareData.dispose();
    }
}
