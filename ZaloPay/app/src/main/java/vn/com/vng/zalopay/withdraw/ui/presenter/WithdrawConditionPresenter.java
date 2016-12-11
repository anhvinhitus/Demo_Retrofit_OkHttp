package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;

import java.util.List;

import javax.inject.Inject;

import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 11/08/2016.
 * Presenter of WithdrawConditionFragment
 */
public class WithdrawConditionPresenter extends AbsWithdrawConditionPresenter<IWithdrawConditionView> {
    private Navigator mNavigator;
    private IListenerValid mIListenerValid;

    @Inject
    WithdrawConditionPresenter(UserConfig userConfig, Navigator navigator) {
        super(userConfig);
        mNavigator = navigator;
        mIListenerValid = new IListenerValid() {
            @Override
            public void onSuccess(List<BankConfig> list, boolean isValid) {
                if (isValid) {
                    if (mView == null) {
                        return;
                    }
                    if (isValidProfile() && mView.getActivity() != null) {
                        mNavigator.startWithdrawActivity(mView.getActivity());
                        mView.getActivity().finish();
                        return;
                    }
                    mView.hideCardNote();
                    mView.hideLoading();
                }
                refreshLinkCard(list);
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
                    public void onOKevent() {
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
        final boolean isProfileValid = isValidProfile();
        mView.setProfileValid(isProfileValid);


        validLinkCard(mIListenerValid);
    }

    @Override
    public void resume() {
        checkConditionAndStartWithdrawActivity();
    }

    @Override
    public void destroy() {
        mIListenerValid = null;
        CShareData.dispose();
        GlobalData.initApplication(null);
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
}
