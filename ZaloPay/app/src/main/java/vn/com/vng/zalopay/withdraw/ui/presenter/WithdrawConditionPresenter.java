package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;

import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import javax.inject.Inject;

import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;

/**
 * Created by longlv on 11/08/2016.
 * Presenter of WithdrawConditionFragment
 */
public class WithdrawConditionPresenter extends AbsWithdrawConditionPresenter<IWithdrawConditionView> {
    private Navigator mNavigator;
    private IListenerValid mIListenerValid;

    @Inject
    WithdrawConditionPresenter(User user, Navigator navigator) {
        super(user);
        mNavigator = navigator;
        mIListenerValid = new IListenerValid() {
            @Override
            public void onSuccess(List<BankConfig> list, boolean isValidLinkCard, boolean isValidLinkAccount) {
                hideLoading();
                if ((isValidLinkCard || isValidLinkAccount)) {
                    if (mView != null && mView.getActivity() != null) {
                        mNavigator.startWithdrawActivityAndFinish(mView.getActivity());
                    }
                } else {
                    refreshLinkCard(list);
                }
            }

            @Override
            public void onError(String error) {
                if (mView == null) {
                    return;
                }
                mView.showConfirmDialog(error, new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        if (mView == null || mView.getActivity() == null) {
                            return;
                        }
                        mView.getActivity().finish();
                    }

                    @Override
                    public void onOKEvent() {
                        checkConditionAndStartWithdrawActivity();
                    }
                });
            }
        };
    }

    private void checkConditionAndStartWithdrawActivity() {
        if (mView == null) {
            return;
        }
        validLinkCard(mIListenerValid);
    }

    @Override
    public void resume() {
        checkConditionAndStartWithdrawActivity();
    }

    @Override
    public void destroy() {
        mIListenerValid = null;
        CShareDataWrapper.dispose();
        super.destroy();
    }

    @Override
    public Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }

    private void refreshLinkCard(List<BankConfig> list) {
        if (mView == null) {
            return;
        }
        mView.refreshListCardSupport(list);
    }

    private void hideLoading() {
        if (mView != null) {
            mView.hideLoading();
        }
    }
}
