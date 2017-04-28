package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;

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
}
