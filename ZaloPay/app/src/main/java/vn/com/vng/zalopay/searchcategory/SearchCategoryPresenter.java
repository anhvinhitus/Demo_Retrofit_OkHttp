package vn.com.vng.zalopay.searchcategory;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Pair;

import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.data.util.InsideAppUtil;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.ObservableHelper;
import vn.com.vng.zalopay.data.util.SearchUtil;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.paymentapps.PaymentAppTypeEnum;
import vn.com.vng.zalopay.ui.subscribe.MerchantUserInfoSubscribe;
import vn.com.vng.zalopay.ui.subscribe.StartPaymentAppSubscriber;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.withdraw.ui.presenter.AbsWithdrawConditionPresenter;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.merchant.entities.Maintenance;

/**
 * Created by khattn on 3/10/17.
 * Search Category Presenter
 */

final class SearchCategoryPresenter extends AbsWithdrawConditionPresenter<ISearchCategoryView> {

    private static final int TIME_DELAY_SEARCH = 300;

    private final MerchantStore.Repository mMerchantRepository;
    private final AppResourceStore.Repository mAppResourceRepository;
    private final FriendStore.Repository mFriendRepository;
    private final Navigator mNavigator;

    private final List<InsideApp> mListApp;
    private PublishSubject<String> mDelaySubject;

    @Inject
    SearchCategoryPresenter(User user,
                            MerchantStore.Repository mMerchantRepository,
                            AppResourceStore.Repository appResourceRepository,
                            FriendStore.Repository friendRepository,
                            Navigator navigator) {
        super(user);
        this.mMerchantRepository = mMerchantRepository;
        this.mAppResourceRepository = appResourceRepository;
        this.mFriendRepository = friendRepository;
        this.mNavigator = navigator;
        this.mListApp = new ArrayList<>();
        initDelayFilterSubject();
    }

    private void initDelayFilterSubject() {
        mDelaySubject = PublishSubject.create();
        Subscription subscription = mDelaySubject.debounce(TIME_DELAY_SEARCH, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String s) {
                        doFilter(s);
                    }
                });
        mSubscription.add(subscription);
    }

    void getListAppResource() {
        Subscription subscription = mAppResourceRepository.getListAppHome()
                .doOnError(Timber::d)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new AppResourceSubscriber());
        mSubscription.add(subscription);
    }

    private class AppResourceSubscriber extends DefaultSubscriber<List<AppResource>> {

        @Override
        public void onNext(List<AppResource> appResources) {
            onGetAppResourceSuccess(appResources);
        }
    }

    private void onGetAppResourceSuccess(List<AppResource> resources) {
        Timber.d("get app resource success - size [%s]", resources.size());

        if (mView == null) {
            return;
        }

        List<InsideApp> insideAppList = InsideAppUtil.getInsideApps();
        mListApp.clear();
        mListApp.addAll(Lists.transform(resources, InsideAppUtil::transform));
        mListApp.addAll(insideAppList);
        mView.refreshInsideApps(getTopRateApp(mListApp, SearchUtil.getTopRateApp()));
    }

    void filter(String s) {
        mDelaySubject.onNext(s);
    }

    private void doFilter(String key) {
        if (TextUtils.isEmpty(key)) {
            mView.showResultView(false, false);
            return;
        }

        Subscription subscription = findAll(key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<Pair<List<InsideApp>, List<ZPProfile>>>() {

                    @Override
                    public void onNext(Pair<List<InsideApp>, List<ZPProfile>> pair) {
                        List<InsideApp> appList = pair.first;
                        List<ZPProfile> friendList = pair.second;
                        if (appList != null && appList.size() != 0) {
                            Timber.d("search list app size [%s] friend size [%s]", appList.size(), friendList.size());
                            mView.setFindResult(appList, friendList, key);
                            mView.showResultView(false, true);
                        } else {
                            mView.showResultView(true, false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.showResultView(true, false);
                        }
                    }
                });
        mSubscription.add(subscription);
    }


    private Observable<Pair<List<InsideApp>, List<ZPProfile>>> findAll(String key) {
        Observable<List<InsideApp>> observableFindApp = searchApp(key);
        Observable<List<ZPProfile>> observableFindFriend = mFriendRepository.findFriends(key);
        return Observable.zip(observableFindApp, observableFindFriend, Pair::create);
    }

    private Observable<List<InsideApp>> searchApp(final String text) {
        return ObservableHelper.makeObservable(() -> {
            List<InsideApp> list = new ArrayList<>();
            String textLowerCase = text.toLowerCase(Locale.getDefault());
            synchronized (mListApp) {
                for (InsideApp app : mListApp) {
                    if (Strings.getIndexOfSearchString(app.appName, textLowerCase) != -1) {
                        list.add(new InsideApp(app));
                    }
                }
            }
            return list;
        });
    }

    void handleLaunchApp(InsideApp app) {
        Timber.d("onclick app %s %s %s %s", app.appType, app.appId, app.insideAppId, app.appName);
        if (app.appType == PaymentAppTypeEnum.REACT_NATIVE.getValue()) {
            if (app.appId == PaymentAppConfig.Constants.RED_PACKET) {
                mNavigator.startMiniAppActivity((Activity) mView.getContext(), ModuleName.RED_PACKET);
            } else {
                AppResource appResource = PaymentAppConfig.getAppResource(app.appId);
                if (appResource == null) {
                    appResource = new AppResource(app.appId);
                }
                startPaymentApp(appResource);
            }
        } else if (app.appType == PaymentAppTypeEnum.WEBVIEW.getValue()) {
            startServiceWebViewActivity(app.appId, app.webUrl);
        } else if (app.appType == PaymentAppTypeEnum.INTERNAL_APP.getValue()) {
            if (app.appId == 0) {
                handleLaunchInternalApp((int) app.insideAppId);
            } else if (app.appId == PaymentAppConfig.Constants.TRANSFER_MONEY) {
                mNavigator.startTransferMoneyActivity((Activity) mView.getContext());
            } else if (app.appId == PaymentAppConfig.Constants.RECEIVE_MONEY) {
                mNavigator.startReceiveMoneyActivity(mView.getContext());
            }
        } else if (app.appType == PaymentAppTypeEnum.INTERNAL_REACT_NATIVE.getValue()) {
            handleLaunchInternalReactApp((int) app.insideAppId, app.moduleName);
        }
    }

    private void handleLaunchInternalApp(int id) {
        switch (id) {
            case InsideApp.Constants.LINK_CARD:
                mNavigator.startLinkCardActivity(mView.getContext());
                break;
            case InsideApp.Constants.RECHARGE:
                mNavigator.startDepositActivity(mView.getContext());
                break;
            case InsideApp.Constants.WITHDRAW:
                startWithdrawActivity();
                break;
            case InsideApp.Constants.PAY_QR_CODE:
                mNavigator.startScanToPayActivity(mView.getContext());
                break;
            case InsideApp.Constants.PROFILE:
                mNavigator.startProfileInfoActivity(mView.getContext());
                break;
            case InsideApp.Constants.BALANCE:
                mNavigator.startBalanceManagementActivity(mView.getContext());
                break;
            case InsideApp.Constants.CHANGE_PIN:
                mNavigator.startChangePinActivity(mView.getContext());
                break;
            case InsideApp.Constants.PROTECT_ACCOUNT:
                mNavigator.startProtectAccountActivity(mView.getContext());
                break;
        }
    }

    private void handleLaunchInternalReactApp(int id, String moduleName) {
        switch (id) {
            case InsideApp.Constants.SUPPORT_CENTER:
                mNavigator.startMiniAppActivity((Activity) mView.getContext(), moduleName);
                break;
            case InsideApp.Constants.NOTIFICATION:
                mNavigator.startMiniAppActivity(getActivity(), moduleName);
                break;
            case InsideApp.Constants.INFORMATION:
                mNavigator.startMiniAppActivity((Activity) mView.getContext(), moduleName);
                break;
            case InsideApp.Constants.HISTORY:
                mNavigator.startTransactionHistoryList(mView.getContext());
                break;
        }
    }

    private boolean isMaintainWithdraw() {
        Maintenance wdMaintenance = SDKApplication.getApplicationComponent()
                .platformInfoInteractor()
                .withdrawMaintain();
        if (wdMaintenance == null || !wdMaintenance.ismaintainwithdraw) {
            return false;
        }
        showMaintainWithdrawDialog(wdMaintenance.maintainwithdrawfrom, wdMaintenance.maintainwithdrawto);
        return true;
    }

    private void showMaintainWithdrawDialog(long maintainFrom, long maintainTo) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm 'ngày' dd/MM/yyyy", Locale.getDefault());
        String maintainWithdrawFrom = simpleDateFormat.format(new Date(maintainFrom));
        String maintainWithdrawTo = simpleDateFormat.format(new Date(maintainTo));
        String message = String.format(mView.getContext().getString(R.string.maintain_withdraw_message),
                maintainWithdrawFrom,
                maintainWithdrawTo);
        mView.showError(message);
    }

    private void startWithdrawActivity() {
        if (mView == null || mView.getContext() == null) {
            return;
        }
        if (isMaintainWithdraw()) {
            return;
        }

        validLinkCard(new AbsWithdrawConditionPresenter.IListenerValid() {
            @Override
            public void onSuccess(List<BankConfig> list,
                                  boolean isValidLinkCard,
                                  boolean isValidLinkAccount) {
                if (mView == null || mView.getContext() == null) {
                    return;
                }
                if (isValidLinkCard || isValidLinkAccount) {
                    mNavigator.startWithdrawActivity(mView.getContext());
                } else {
                    mNavigator.startWithdrawConditionActivity(mView.getContext());
                }
            }

            @Override
            public void onError(String error) {
                if (mView == null || mView.getContext() == null) {
                    return;
                }
                mView.showConfirmDialog(error,
                        mView.getContext().getString(R.string.txt_retry),
                        mView.getContext().getString(R.string.txt_close),
                        new ZPWOnEventConfirmDialogListener() {
                            @Override
                            public void onCancelEvent() {

                            }

                            @Override
                            public void onOKEvent() {
                                startWithdrawActivity();
                            }
                        });
            }
        });

    }

    private void startServiceWebViewActivity(long appId, String webViewUrl) {
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MerchantUserInfoSubscribe(mNavigator, (Activity) mView.getContext(), appId, webViewUrl));
        mSubscription.add(subscription);
    }

    private void startPaymentApp(AppResource app) {
        Subscription subscription = mAppResourceRepository.isAppResourceAvailable(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(mNavigator, (Activity) mView.getContext(), app));
        mSubscription.add(subscription);
    }

    @Override
    public Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return (Activity) mView.getContext();
    }

    private List<InsideApp> getTopRateApp(List<InsideApp> pAllApp, int pNumber) {
        List<InsideApp> TopRateApp = new ArrayList<InsideApp>();
        for (int i = 0; i < pNumber; i++) {
            TopRateApp.add(pAllApp.get(i));
        }
        return TopRateApp;
    }
}
