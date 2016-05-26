package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;

/**
 * Created by AnhHieu on 5/11/16.
 */
public interface ILinkCardProduceView extends ILoadDataView {
    Activity getActivity();
    void onAddCardSuccess(DMappedCard card);
    void onTokenInvalid();
}
