package vn.com.vng.zalopay.bank.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.zalopay.ui.widget.dialog.SweetAlertDialog;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.bank.BankUtils;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.network.NetworkConnectionException;
import vn.com.vng.zalopay.network.NetworkHelper;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.zalopay.utility.PlayStoreUtils;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.BankAccount;
import vn.com.zalopay.wallet.constants.BankStatus;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;

/**
 * Created by Duke on 5/25/17.
 * List support bank.
 */
final class BankSupportSelectionPresenter extends AbstractPresenter<IBankSupportSelectionView> {
    private final User mUser;
    protected final Context applicationContext;

    @Inject
    BankSupportSelectionPresenter(Context applicationContext, User user) {
        this.applicationContext = applicationContext;
        this.mUser = user;
    }

    void listBankSupport() {
        Subscription subscription = SDKApplication
                .getApplicationComponent()
                .bankListInteractor()
                .getSupportBanks(BuildConfig.VERSION_NAME, System.currentTimeMillis())
                .compose(SchedulerHelper.applySchedulers())
                .subscribe(new BankSupportSubscriber());
        mSubscription.add(subscription);
    }

    void linkBank(ZPBank bank) {
        if (!NetworkHelper.isNetworkAvailable(applicationContext)) {
            mView.showNetworkErrorDialog();
            return;
        }

        if (bank.bankStatus == BankStatus.MAINTENANCE) {
            mView.showMessageDialog(bank.bankMessage, null);
        } else if (bank.bankStatus == BankStatus.UPVERSION) {
            mView.showConfirmDialog(bank.bankMessage,
                    applicationContext.getString(R.string.txt_update),
                    applicationContext.getString(R.string.txt_close),
                    new ZPWOnEventConfirmDialogListener() {
                        @Override
                        public void onCancelEvent() {
                        }

                        @Override
                        public void onOKEvent() {
                            if (mView == null || mView.getActivity() == null) {
                                return;
                            }

                            PlayStoreUtils.openPlayStoreForUpdate(mView.getActivity(), BuildConfig.PACKAGE_IN_PLAY_STORE,
                                    applicationContext.getString(R.string.app_name), "force-app-update", "bank-future");
                        }
                    });
        } else if (bank.isBankAccount()) {
            linkAccount(bank.bankCode);
        } else {
            linkCard(bank.bankCode);
        }
    }

    private void linkCard(String cardCode) {
        linkBank(Constants.LinkBank.LINK_CARD, cardCode);
    }

    private void linkAccount(String cardCode) {
        Subscription subscription = Observable.defer(() -> {
            List<BankAccount> mapCardList = CShareDataWrapper.getMapBankAccountList(mUser);
            return Observable.just(mapCardList);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<List<BankAccount>>() {
                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.showError(ErrorMessageFactory.create(applicationContext, e));
                        }
                    }

                    @Override
                    public void onNext(List<BankAccount> bankAccounts) {
                        if (BankUtils.isLinkedBankAccount(bankAccounts, cardCode)) {
                            showVCBWarningDialog();
                        } else {
                            showVCBConfirmDialog(cardCode);
                        }
                    }
                });

        mSubscription.add(subscription);
    }

    protected void showVCBWarningDialog() {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(mView.getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);
        dialog.setTitleText(applicationContext.getString(R.string.notification));
        dialog.setContentText(applicationContext.getString(R.string.bank_link_account_vcb_exist));
        dialog.setConfirmText(applicationContext.getString(R.string.txt_close));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> dialog.dismiss());
        dialog.show();
    }

    protected void showVCBConfirmDialog(String cardCode) {
        if (mView == null) return;
        SweetAlertDialog dialog = new SweetAlertDialog(mView.getActivity(), SweetAlertDialog.NORMAL_TYPE, R.style.alert_dialog);

        dialog.setTitleText(applicationContext.getString(R.string.notification));
        dialog.setCancelText(applicationContext.getString(R.string.txt_cancel));
        dialog.setContentText(getVCBWarningMessage());
        dialog.setConfirmText(applicationContext.getString(R.string.accept));
        dialog.setConfirmClickListener((SweetAlertDialog sweetAlertDialog) -> {
            setResultDoLinkAccount(cardCode);
            dialog.dismiss();
        });
        dialog.show();
    }

    private String getVCBWarningMessage() {
        return String.format(applicationContext.getString(R.string.link_account_empty_bank_support_phone_require_hint),
                "<b>" + PhoneUtil.formatPhoneNumberWithDot(mUser.phonenumber) + "</b>") +
                "<br><br>" +
                applicationContext.getString(R.string.link_account_empty_bank_support_balance_require_hint);
    }

    private void setResultDoLinkAccount(String cardCode) {
        linkBank(Constants.LinkBank.LINK_ACCOUNT, cardCode);
    }

    private void linkBank(int type, String cardCode) {
        if (mView == null || mView.getActivity() == null) {
            return;
        }

        Activity activity = mView.getActivity();
        Intent data = new Intent();
        data.putExtra("type", type);

        if (!TextUtils.isEmpty(cardCode)) {
            data.putExtra(Constants.BANK_DATA_RESULT_AFTER_LINK, cardCode);
        }

        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }


    private class BankSupportSubscriber extends DefaultSubscriber<List<ZPBank>> {
        BankSupportSubscriber() {
        }

        @Override
        public void onStart() {
            if (mView != null) {
                mView.showLoading();
            }
        }

        @Override
        public void onError(Throwable e) {
            if (mView == null) {
                return;
            }


            String message = ErrorMessageFactory.create(applicationContext, e);
            if (message.equals(applicationContext.getString(R.string.exception_generic))) {
                message = applicationContext.getString(R.string.bank_error_load_supportbank);
            }

            int dialogType = SweetAlertDialog.ERROR_TYPE;
            if (e instanceof NetworkConnectionException) {
                dialogType = SweetAlertDialog.NO_INTERNET;
            }

            Timber.w("Get bank support error %s", e.toString());

            mView.hideLoading();
            mView.showDialogThenClose(message, applicationContext.getString(R.string.txt_close), dialogType);
        }

        @Override
        public void onCompleted() {
            if (mView != null) {
                mView.hideLoading();
            }
        }

        @Override
        public void onNext(List<ZPBank> banks) {
            if (mView == null) {
                return;
            }

            mView.hideLoading();
            mView.setData(banks);
        }
    }
}