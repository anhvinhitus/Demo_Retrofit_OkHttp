package vn.com.zalopay.wallet.voucher;

/**
 * Created by chucvv on 8/2/17.
 */

public interface IInteractVoucher {
    void onClose();
    void onVoucherInfoComplete(String voucherCode);
}
