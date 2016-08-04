package vn.com.vng.zalopay.ui.view;

import android.app.Activity;

import java.util.List;

import vn.com.vng.zalopay.domain.model.BankCard;
import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;

/**
 * Created by AnhHieu on 5/11/16.
 */
public interface ILinkCardView extends ILoadDataView {

    Activity getActivity();

    void setData(List<BankCard> bankCards);

    void updateData(BankCard bankCard);

    void removeData(BankCard bankCard);

    void onAddCardSuccess(DMappedCard card);

    void onTokenInvalid();
}
