package vn.com.zalopay.wallet.view.custom.cardview.pager;

public interface IActionListener {
    void onActionComplete(CreditCardFragment fragment);

    void onEdit(CreditCardFragment fragment, String edit);
}