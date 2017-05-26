package vn.zalopay.promotion;

import com.zalopay.ui.widget.UIBottomSheetDialog;

public class CashBackBuilder extends PromotionBuilder {
    @Override
    public UIBottomSheetDialog.IRender build() {
        return new CashBackRender(this);
    }
}
