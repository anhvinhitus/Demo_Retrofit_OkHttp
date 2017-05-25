package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.models.LinkBankType;
import vn.com.vng.zalopay.data.balance.BalanceStore;
import vn.com.vng.zalopay.data.transaction.TransactionStore;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.repository.ZaloPayRepository;
import vn.com.vng.zalopay.react.error.PaymentError;
import vn.com.vng.zalopay.service.DefaultPaymentResponseListener;
import vn.com.vng.zalopay.service.PaymentWrapper;
import vn.com.vng.zalopay.service.PaymentWrapperBuilder;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.ui.view.ILoadDataView;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.wallet.business.entity.base.ZPPaymentResult;
import vn.com.zalopay.wallet.business.entity.base.ZPWPaymentInfo;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DMappedCard;
import vn.com.zalopay.wallet.business.entity.user.UserInfo;
import vn.com.zalopay.wallet.listener.ZPWOnEventConfirmDialogListener;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;
import vn.com.zalopay.wallet.view.dialog.SweetAlertDialog;

/**
 * Created by Duke on 5/25/17.
 */
public class BankSupportSelectionPresenter extends AbstractPresenter<IBankSupportSelectionView> {
    private PaymentWrapper paymentWrapper;
    private final User mUser;
    private LinkBankType mBankType;
    private DefaultSubscriber<List<ZPCard>> mGetSupportBankSubscriber;

    @Inject
    BankSupportSelectionPresenter(User user,
                                  BalanceStore.Repository balanceRepository,
                                  TransactionStore.Repository transactionRepository,
                                  ZaloPayRepository zaloPayRepository) {
        this.mUser = user;
        mGetSupportBankSubscriber = new DefaultSubscriber<List<ZPCard>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.d("Get support bank type [%s] onError [%s]", mBankType, e.getMessage());
                showRetryDialog();
            }

            @Override
            public void onNext(List<ZPCard> cardList) {
                Timber.d("Get support bank type [%s] onComplete list card [%s]", mBankType, cardList);
                fetchListBank(cardList);
            }
        };

        paymentWrapper = new PaymentWrapperBuilder()
                .setBalanceRepository(balanceRepository)
                .setZaloPayRepository(zaloPayRepository)
                .setTransactionRepository(transactionRepository)
                .setResponseListener(new DefaultPaymentResponseListener() {
                    @Override
                    protected ILoadDataView getView() {
                        return null;
                    }
                })
                .build();
    }

    @Override
    public void attachView(IBankSupportSelectionView iBankSupportSelectionView) {
        super.attachView(iBankSupportSelectionView);
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

    private void fetchListBank(List<ZPCard> listBank) {
        if (mView != null) {
            mView.fetchListBank(listBank);
        }
    }

    void getCardSupport() {
        Timber.d("Get list bank support %s", mBankType);
        UserInfo userInfo = new UserInfo();
        userInfo.zaloPayUserId = mUser.zaloPayId;
        userInfo.accessToken = mUser.accesstoken;
        Subscription subscription = CShareDataWrapper.getCardSupportList(userInfo, mGetSupportBankSubscriber);
        mSubscription.add(subscription);
    }

    void iniData(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mBankType = (LinkBankType) bundle.getSerializable(Constants.ARG_LINK_BANK_TYPE);
    }

    void showVCBWarningDialog() {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(mView.getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

        dialog.setTitleText(mView.getActivity().getString(R.string.notification));
        dialog.setCancelText(mView.getActivity().getString(R.string.txt_cancel));
        dialog.setContentText(getVCBWarningMessage());
        dialog.setConfirmText(mView.getActivity().getString(R.string.accept));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
            paymentWrapper.linkAccount(mView.getActivity(), "ZPVCB");
            dialog.dismiss();
        });
        dialog.show();
    }

    String getVCBWarningMessage() {
        StringBuilder message = new StringBuilder();
        message.append(String.format(mView.getActivity().getString(R.string.link_account_empty_bank_support_phone_require_hint),
                PhoneUtil.formatPhoneNumberWithSpace(mUser.phonenumber)));
        message.append(String.format("%n%n", ""));
        message.append(mView.getActivity().getString(R.string.link_account_empty_bank_support_balance_require_hint));

        return message.toString();
    }

    void linkCard() {
        paymentWrapper.linkCard(mView.getActivity());
    }
}
