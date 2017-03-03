package vn.com.zalopay.wallet.business.entity.base;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.enumeration.EPaymentChannel;
import vn.com.zalopay.wallet.utils.Log;

public class ZPPaymentOption extends BaseEntity<ZPPaymentOption> {

    /**
     * Payment method type user wants to directly enter
     */
    private EPaymentChannel includePaymentMethodType = null;

    /**
     * Payment method types user wants to disable
     */
    private List<EPaymentChannel> excludePaymentMethodTypes = null;
    private HashSet<String> mExcludePmcIdSet = null;

    public ZPPaymentOption() {
    }

    public ZPPaymentOption(EPaymentChannel includeChannel) {
        this.includePaymentMethodType = includeChannel;
    }

    public static ZPPaymentOption fromJson(String pJson) {
        return (new Gson()).fromJson(pJson, ZPPaymentOption.class);
    }

    public String getIncludePaymentMethodType() {
        if (includePaymentMethodType == null)
            return null;

        String pmcIDStr = GlobalData.getStringResource(includePaymentMethodType.toString());
        return pmcIDStr;
    }

    public HashSet<String> getExcludePaymentMethodTypes() {
        if (excludePaymentMethodTypes == null)
            return null;

        if (mExcludePmcIdSet == null) {
            mExcludePmcIdSet = new HashSet<String>();
            for (EPaymentChannel paymentMethodType : excludePaymentMethodTypes) {
                String pmcIDStr = GlobalData.getStringResource(paymentMethodType.toString());
                try {
                    mExcludePmcIdSet.add(pmcIDStr);
                } catch (Exception ex) {
                    Log.e(this, ex);
                }
            }
        }
        return mExcludePmcIdSet;
    }
}
