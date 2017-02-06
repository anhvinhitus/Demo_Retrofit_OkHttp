package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.bank.models.BankAssociatePagerIndex;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.IPresenter;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by longlv on 2/6/17.
 * *
 */

class BankAssociatePresenter implements IPresenter<IBankAssociateView> {

    IBankAssociateView mView;

    @Inject
    User mUser;

    @Inject
    BankAssociatePresenter() {

    }

    @Override
    public void attachView(IBankAssociateView iBankAssociateView) {
        mView = iBankAssociateView;
    }

    void initPageStart(Bundle bundle) {
        if (changePageInBundle(bundle)) {
            Timber.d("Init page in bundle.");
        } else {
            changePageInContext();
            Timber.d("Init page in context.");
        }
    }

    private boolean changePageInBundle(Bundle bundle) {
        if (bundle == null) {
            return false;
        }
        int pageIndex = bundle.getInt(Constants.ARG_PAGE_INDEX, -1);
        return mView.setCurrentPage(pageIndex);
    }

    /**
     * Khi User launch màn hình này, tab được hiển thị đầu tiên, được xác định theo rule sau:
     * 1) Nếu User chưa có liên kết thẻ, liên kết tài khoản: Hiển thị tab mặc định là "Thẻ"
     * 2) Nếu User đã có liên kết thẻ, chưa có liên kết tài khoản: Hiển thị tab "Thẻ"
     * 3) Nếu User đã có liên kết tài khoản, chưa có liên kết thẻ: Hiển thị tab "Tài khoản"
     * 4) Nếu User đã có cả 2 liên kết thẻ và tài khoản: Hiển thị tab "Thẻ"
     * Reference: https://gitlab.com/zalopay/bugs/issues/273
     */
    void changePageInContext() {
        ObservableHelper.makeObservable(new Callable<BankAssociatePagerIndex>() {
            @Override
            public BankAssociatePagerIndex call() throws Exception {
                List<DMappedCard> mapCardList = CShareData.getInstance().getMappedCardList(mUser.zaloPayId);
                List<DBankAccount> mapAccList = CShareData.getInstance().getMapBankAccountList(mUser.zaloPayId);
                if (Lists.isEmptyOrNull(mapCardList) && Lists.isEmptyOrNull(mapAccList)) {
                    return BankAssociatePagerIndex.LINK_CARD;
                } else if (!Lists.isEmptyOrNull(mapCardList)) {
                    return BankAssociatePagerIndex.LINK_CARD;
                } else if (!Lists.isEmptyOrNull(mapAccList)) {
                    return BankAssociatePagerIndex.LINK_ACCOUNT;
                } else {
                    return BankAssociatePagerIndex.LINK_CARD;
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ChangePageInContextSubscriber());

    }

    @Override
    public void detachView() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    private class ChangePageInContextSubscriber extends DefaultSubscriber<BankAssociatePagerIndex> {

        @Override
        public void onNext(BankAssociatePagerIndex pageInContext) {
            if (mView == null || pageInContext == null) {
                return;
            }
            mView.setCurrentPage(BankAssociatePagerIndex.LINK_ACCOUNT.getValue());
        }

        @Override
        public void onError(Throwable e) {
            Timber.w(e, "Change page in context throw exception.");
            super.onError(e);
        }
    }
}
