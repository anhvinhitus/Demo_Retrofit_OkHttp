package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;

import javax.inject.Inject;

import vn.com.vng.zalopay.data.cache.UserConfig;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardType;

/**
 * Created by longlv on 11/08/2016.
 * Presenter of WithdrawConditionFragment
 */
public class WithdrawConditionPresenter extends AbsWithdrawConditionPresenter
        implements IPresenter<IWithdrawConditionView> {

    private IWithdrawConditionView mView;
    private Navigator mNavigator;

    @Inject
    public WithdrawConditionPresenter(UserConfig userConfig, Navigator navigator) {
        super(userConfig);
        mNavigator = navigator;
    }

    private void checkConditionAndStartWithdrawActivity() {
        if (mView == null) {
            return;
        }
        boolean isProfileValid = isValidProfile();
        mView.setProfileValid(isProfileValid);
        if (isProfileValid && isValidLinkCard()) {
            mNavigator.startWithdrawActivity(mView.getContext());
            mView.getActivity().finish();
        }
    }

    @Override
    public void setView(IWithdrawConditionView view) {
        mView = view;
    }

    @Override
    public void destroyView() {
        mView = null;
    }

    @Override
    public void resume() {
        checkConditionAndStartWithdrawActivity();
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        GlobalData.initApplication(null);
    }
    @Override
    public Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }

    @Override
    public void setBankValid(String bankCode, boolean isValid) {
        if (mView == null) {
            return;
        }
        if (isValid) {
            mView.hideCardNote();
        }
        if (ECardType.PVTB.toString().equals(bankCode)) {
            mView.setChkVietinBank(isValid);
        } else if (ECardType.PSCB.toString().equals(bankCode)) {
            mView.setChkSacomBank(isValid);
        }
    }

}
