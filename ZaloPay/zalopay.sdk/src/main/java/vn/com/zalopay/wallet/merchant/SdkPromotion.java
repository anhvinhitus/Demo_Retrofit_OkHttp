package vn.com.zalopay.wallet.merchant;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.text.TextUtils;
import android.view.View;

import com.zalopay.ui.widget.UIBottomSheetDialog;

import timber.log.Timber;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.objectmanager.SingletonBase;
import vn.zalopay.promotion.CashBackRender;
import vn.zalopay.promotion.IBuilder;
import vn.zalopay.promotion.IInteractPromotion;
import vn.zalopay.promotion.IPromotionResult;
import vn.zalopay.promotion.IResourceLoader;
import vn.zalopay.promotion.PromotionEvent;

/*
 * Created by chucvv on 7/24/17.
 */

public class SdkPromotion extends SingletonBase {
    private static SdkPromotion _object;
    IBuilder mPromotionBuilder;
    IPromotionResult mPromotionResult;
    Context mContext;
    private String mTransactionID;

    private SdkPromotion() {
        super();
    }

    public static SdkPromotion shared() {
        if (_object == null) {
            _object = new SdkPromotion();
        }
        return _object;
    }

    SdkPromotion plant(Context pContext) {
        this.mContext = pContext;
        return this;
    }

    public SdkPromotion setTransId(String pTransId) {
        this.mTransactionID = pTransId;
        return this;
    }

    void handlePromotion(Object[] pAdditionParams) {
        Timber.d("got promotion from notification");
        if (pAdditionParams == null || pAdditionParams.length <= 0) {
            Timber.d("stopping processing promotion from notification because of empty pAdditionParams");
            return;
        }
        PromotionEvent promotionEvent = null;
        if (pAdditionParams[0] instanceof PromotionEvent) {
            promotionEvent = (PromotionEvent) pAdditionParams[0];
        }
        if (mPromotionBuilder != null) {
            Log.d(this, "promotion event is updated", promotionEvent);
            mPromotionBuilder.setPromotion(promotionEvent);
            return;
        }
        if (promotionEvent == null) {
            Timber.d("stopping processing promotion from notification because promotion event is null");
            return;
        }
        if (pAdditionParams.length >= 2 && pAdditionParams[1] instanceof IPromotionResult) {
            mPromotionResult = (IPromotionResult) pAdditionParams[1];
        }

        long transId = -1;
        if (!TextUtils.isEmpty(mTransactionID)) {
            try {
                transId = Long.parseLong(mTransactionID);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (transId == -1) {
            Timber.d("stopping processing promotion from notification because transid is not same");
            if (mPromotionResult != null) {
                mPromotionResult.onReceiverNotAvailable();//callback again to notify that sdk don't accept this notification
            }
            return;
        }

        IResourceLoader resourceLoader = null;
        if (pAdditionParams.length >= 3 && pAdditionParams[2] instanceof IResourceLoader) {
            resourceLoader = (IResourceLoader) pAdditionParams[2];
        }
        View contentView = View.inflate(mContext, vn.zalopay.promotion.R.layout.layout_promotion_cash_back, null);
        mPromotionBuilder = CashBackRender.getBuilder()
                .setPromotion(promotionEvent)
                .setView(contentView)
                .setResourceProvider(resourceLoader)
                .setInteractPromotion(new IInteractPromotion() {
                    @Override
                    public void onUserInteract(PromotionEvent pPromotionEvent) {
                        if (mPromotionResult != null) {
                            try {
                                mPromotionResult.onNavigateToAction(mContext, pPromotionEvent);
                            } catch (Exception e) {
                                Log.e(this, e);
                            }
                        }
                    }

                    @Override
                    public void onClose() {
                        mPromotionResult = null;
                        mPromotionBuilder.release();
                        mPromotionBuilder = null;
                    }
                });
        try {
            UIBottomSheetDialog bottomSheetDialog = new UIBottomSheetDialog(mContext, vn.zalopay.promotion.R.style.CoffeeDialog, mPromotionBuilder.build());
            bottomSheetDialog.show();
            bottomSheetDialog.setState(BottomSheetBehavior.STATE_EXPANDED);
        } catch (Exception e) {
            Timber.w(e, "Exception show promotion view");
        }
    }
}