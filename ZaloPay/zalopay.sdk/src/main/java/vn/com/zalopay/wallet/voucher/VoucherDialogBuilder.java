package vn.com.zalopay.wallet.voucher;

import android.view.View;

import com.zalopay.ui.widget.IUIBottomSheetBuilder;
import com.zalopay.ui.widget.UIBottomSheetDialog;

public class VoucherDialogBuilder implements IVoucherDialogBuilder {
    protected View mView;
    private IInteractVoucher mInteractVoucher;
    @Override
    public View getView() {
        return mView;
    }

    @Override
    public VoucherDialogBuilder setView(View pView) {
        this.mView = pView;
        return this;
    }

    @Override
    public UIBottomSheetDialog.IRender build() {
        return new VoucherRender(this);
    }

    @Override
    public void release() {
        mView = null;
    }

    @Override
    public IUIBottomSheetBuilder setInteractListener(IInteractVoucher interactVoucher) {
        this.mInteractVoucher = interactVoucher;
        return this;
    }

    @Override
    public IInteractVoucher getInteractListener() {
        return mInteractVoucher;
    }
}
