package vn.zalopay.promotion;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public class VoucherBuilder extends PromotionBuilder {
    @Override
    public UIBottomSheetDialog.IRender build() {
        return new VoucherRender(this);
    }
}
