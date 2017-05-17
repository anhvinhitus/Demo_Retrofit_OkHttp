package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.zalopay.ui.widget.IconFontDrawable;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.DialogHelper;

/**
 * Created by longlv on 4/28/17.
 * Common function of LinkCardFragment & LinkAccountFragment
 */

public abstract class AbstractLinkBankFragment extends BaseFragment {

    protected void showConfirmPayAfterLinkBank(LinkBankType linkBankType) {
        String message = getString(R.string.confirm_continue_pay_after_link_card);
        if (LinkBankType.LINK_BANK_ACCOUNT.equals(linkBankType)) {
            message = getString(R.string.confirm_continue_pay_after_link_account);
        }
        DialogHelper.showNoticeDialog(getActivity(),
                message,
                getString(R.string.btn_continue),
                getString(R.string.btn_cancel_transaction),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        getActivity().setResult(Constants.RESULT_END_PAYMENT);
                        getActivity().finish();
                    }

                    @Override
                    public void onOKevent() {
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }
                });
    }

    protected SwipeMenuCreator mSwipeMenuCreator = (swipeLeftMenu, swipeRightMenu, viewType) -> {
        int width = getResources().getDimensionPixelSize(R.dimen.link_card_remove_width);
        int height = ViewGroup.LayoutParams.MATCH_PARENT;

        IconFontDrawable iconFontDrawable = new IconFontDrawable(getContext());
        iconFontDrawable.setIcon(R.string.general_delete_card);
        iconFontDrawable.setColor(Color.WHITE);
        iconFontDrawable.setResourcesSize(R.dimen.font_size_delete);

        SwipeMenuItem deleteItem = new SwipeMenuItem(getContext())
                .setBackgroundDrawable(R.color.red)
                .setText(getString(R.string.delete))
                .setImage(iconFontDrawable)
                .setTextColor(Color.WHITE)
                .setWidth(width)
                .setHeight(height);
        swipeRightMenu.addMenuItem(deleteItem);
    };
}
