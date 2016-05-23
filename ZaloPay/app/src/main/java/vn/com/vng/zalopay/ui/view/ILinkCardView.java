package vn.com.vng.zalopay.ui.view;

import java.util.List;

import vn.com.vng.zalopay.domain.model.BankCard;

/**
 * Created by AnhHieu on 5/11/16.
 */
public interface ILinkCardView extends ILoadDataView {
    void setData(List<BankCard> bankCards);

    void updateData(BankCard bankCard);

    void removeData(BankCard bankCard);
}
