package vn.com.zalopay.wallet.voucher;

import com.zalopay.ui.widget.IUIBottomSheetBuilder;

/*
 * Created by chucvv on 8/2/17.
 */

public interface IVoucherDialogBuilder extends IUIBottomSheetBuilder<IVoucherDialogBuilder> {
    IUIBottomSheetBuilder setInteractListener(IInteractVoucher interactVoucher);

    IInteractVoucher getInteractListener();
}
