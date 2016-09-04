package vn.com.vng.zalopay.withdraw.ui.presenter;

import android.text.TextUtils;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.BaseUserPresenter;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.vng.zalopay.withdraw.ui.view.IWithdrawConditionView;
import vn.com.zalopay.wallet.data.GlobalData;
import vn.com.zalopay.wallet.entity.enumeration.ECardType;
import vn.com.zalopay.wallet.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 11/08/2016.
 * Presenter of WithdrawConditionFragment
 */
public class WithdrawConditionPresenter extends BaseUserPresenter implements IPresenter<IWithdrawConditionView> {

    private IWithdrawConditionView mView;

    CompositeSubscription compositeSubscription = new CompositeSubscription();

    private void getUserProfileFromServer() {
        Subscription subscription = accountRepository.getUserProfileLevelCloud()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ProfileSubscriber());
        compositeSubscription.add(subscription);
    }

    private class ProfileSubscriber extends DefaultSubscriber<Boolean> {

        @Override
        public void onCompleted() {
            WithdrawConditionPresenter.this.checkConditionAndStartWithdrawActivity();
        }

        @Override
        public void onError(Throwable e) {
        }
    }

    private boolean isValidProfileLevel() {
        User user = userConfig.getCurrentUser();
        if (user == null) {
            return false;
        }
        boolean isValid = true;
        if (!TextUtils.isEmpty(user.email)) {
            mView.setChkEmail(true);
        } else {
            isValid = false;
        }
        if (!TextUtils.isEmpty(user.identityNumber)) {
            mView.setChkIdentityNumber(true);
        } else {
            isValid = false;
        }
        return isValid;
    }

    private boolean isValidLinkCard() {
        User user = userConfig.getCurrentUser();
        boolean isMapped = false;
        try {
            List<DMappedCard> mapCardLis = CShareData.getInstance(mView.getActivity()).getMappedCardList(user.zaloPayId);
            if (mapCardLis == null || mapCardLis.size() <= 0) {
                return false;
            }
            for (int i = 0; i < mapCardLis.size(); i++) {
                DMappedCard card = mapCardLis.get(i);
                if (card == null || TextUtils.isEmpty(card.bankcode)) {
                    continue;
                }
                if (ECardType.PVTB.toString().equals(card.bankcode)) {
                    mView.setChkVietinBank(true);
                    isMapped = true;
                } else if (ECardType.PSCB.toString().equals(card.bankcode)) {
                    mView.setChkSacomBank(true);
                    isMapped = true;
                }
            }
            return isMapped;
        } catch (Exception e) {
            Timber.w(e, "Get mapped card exception: %s", e.getMessage());
        }
        return isMapped;
    }

    public boolean isValidCondition() {
        boolean isValidProfile = isValidProfileLevel();
        boolean isValidLinkCard = isValidLinkCard();
        boolean isValidCondition = isValidProfile && isValidLinkCard;
        if (isValidProfile) {
            mView.hideUpdateProfile();
            mView.hideUserNote();
        } else {
            if (userConfig.isWaitingApproveProfileLevel3()) {
                mView.hideUpdateProfile();
                mView.showUserNote();
            } else {
                mView.showUpdateProfile();
                mView.hideUserNote();

            }
            User user = userConfig.getCurrentUser();
            if (user.profilelevel >= 3 &&
                    (TextUtils.isEmpty(user.identityNumber) || TextUtils.isEmpty(user.email))) {
                getUserProfileFromServer();
            }
        }

        return isValidCondition;
    }

    private void checkConditionAndStartWithdrawActivity() {
        if (mView == null) {
            return;
        }
        if (isValidCondition()) {
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
        unsubscribeIfNotNull(compositeSubscription);
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
}
