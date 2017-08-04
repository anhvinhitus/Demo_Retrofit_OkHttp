package vn.com.vng.zalopay.promotion;

import android.app.Activity;
import android.content.Context;

import com.zalopay.apploader.internal.ModuleName;

import timber.log.Timber;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.zalopay.promotion.ActionType;
import vn.zalopay.promotion.model.PromotionEvent;

/**
 * Created by chucvv on 5/27/17.
 */

public class PromotionHelper {
    private Navigator mNavigator;

    public PromotionHelper(Navigator pNavigator) {
        this.mNavigator = pNavigator;
        Timber.d("create PromotionHelper for promotion");
    }

    public void navigate(Context pContext, PromotionEvent pPromotionEvent) throws Exception {
        if (pPromotionEvent == null || pPromotionEvent.actions == null || pPromotionEvent.actions.isEmpty()) {
            return;
        }
        if (mNavigator == null) {
            return;
        }
        switch (pPromotionEvent.actions.get(0).action) {
            case ActionType.TRANSACTION_DETAIL:
                if (pPromotionEvent.notificationId > 0) {
                    mNavigator.startTransactionDetail(pContext, String.valueOf(pPromotionEvent.transid), String.valueOf(pPromotionEvent.notificationId));
                } else {
                    mNavigator.startMiniAppActivity((Activity) pContext, ModuleName.NOTIFICATIONS);
                }
                break;
            case ActionType.VOUCHER_LIST:
                mNavigator.startVoucherApp((Activity) pContext);
                break;
            default:
                Timber.d("undefine action on promotion");
        }
    }
}
