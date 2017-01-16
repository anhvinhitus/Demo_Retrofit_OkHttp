package vn.com.vng.zalopay.linkcard.ui;

import java.util.ArrayList;

import javax.inject.Inject;

import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.CShareData;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.merchant.listener.IGetCardSupportListListener;

/**
 * Created by longlv on 1/16/17.
 * *
 */

class BankSupportPresenter extends AbstractPresenter<IBankSupportView> {

    private IGetCardSupportListListener mGetCardSupportListListener;

    @Inject
    User mUser;

    @Inject
    BankSupportPresenter() {

    }

    @Override
    public void attachView(IBankSupportView iSupportLinkAccView) {
        super.attachView(iSupportLinkAccView);
        mGetCardSupportListListener = new IGetCardSupportListListener() {
            @Override
            public void onProcess() {
                Timber.d("getCardSupportList onProcess");
            }

            @Override
            public void onComplete(ArrayList<ZPCard> cardSupportList) {
                if (mView == null) {
                    return;
                }

                Timber.d("getCardSupportList onComplete cardSupportList[%s]", cardSupportList);
                mView.refreshBankSupports(cardSupportList);
            }

            @Override
            public void onError(String pErrorMess) {
                Timber.d("cardSupportHashMap onError [%s]", pErrorMess);
                hideLoading();
                showRetryDialog();
            }

            @Override
            public void onUpVersion(boolean forceUpdate, String latestVersion, String message) {
                if (mView == null) {
                    return;
                }
                mView.onEventUpdateVersion(forceUpdate, latestVersion, message);
            }
        };
    }

    void getCardSupport() {
        Timber.d("Get card support");
        showLoading();
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        CShareData.getInstance().setUserInfo(userInfo).getCardSupportList(mGetCardSupportListListener);
    }

    @Override
    public void destroy() {
        super.destroy();
        mGetCardSupportListListener = null;
    }

    private void showRetryDialog() {
        if (mView == null || mView.getContext() == null) {
            return;
        }

        mView.showRetryDialog(mView.getContext().getString(R.string.exception_generic), new ZPWOnEventConfirmDialogListener() {
            @Override
            public void onCancelEvent() {

            }

            @Override
            public void onOKevent() {
                getCardSupport();
            }
        });
    }

    public void showLoading() {
        if (mView != null) {
            mView.showLoading();
        }
    }

    public void hideLoading() {
        if (mView != null) {
            mView.hideLoading();
        }
    }
}
