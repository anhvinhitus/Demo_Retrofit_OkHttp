package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.app.Activity;
import android.text.TextUtils;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
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

    @Inject
    public WithdrawConditionPresenter() {
    }

    private void checkConditionAndStartWithdrawActivity() {
        if (mView == null) {
            return;
        }
        boolean isProfileValid = isValidProfile();
        mView.setProfileValid(isProfileValid);
        if (isProfileValid && isValidLinkCard()) {
            navigator.startWithdrawActivity(mView.getContext());
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
