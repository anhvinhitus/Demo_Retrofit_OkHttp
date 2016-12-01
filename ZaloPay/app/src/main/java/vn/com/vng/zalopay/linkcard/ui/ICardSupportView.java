package vn.com.vng.zalopay.linkcard.ui;

import android.app.Activity;

import java.util.List;

import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;

/**
 * Created by longlv on 10/22/16.
 * *
 */
interface ICardSupportView extends ILoadDataView {

    Activity getActivity();

    void onTokenInvalid();

    void onPreComplete();

    void showWarningView(String error);
}
