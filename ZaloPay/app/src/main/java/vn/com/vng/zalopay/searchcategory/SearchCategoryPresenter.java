package vn.com.vng.zalopay.searchcategory;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import com.zalopay.apploader.internal.ModuleName;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.api.ResponseHelper;
import vn.com.vng.zalopay.data.appresources.AppResourceStore;
import vn.com.vng.zalopay.data.merchant.MerchantStore;
import vn.com.vng.zalopay.data.util.InsideAppUtil;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zfriend.FriendStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.InsideApp;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.exception.ErrorMessageFactory;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.paymentapps.PaymentAppConfig;
import vn.com.vng.zalopay.paymentapps.PaymentAppTypeEnum;
import vn.com.vng.zalopay.ui.subscribe.MerchantUserInfoSubscribe;
import vn.com.vng.zalopay.ui.subscribe.StartPaymentAppSubscriber;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.CShareDataWrapper;
import vn.com.vng.zalopay.withdraw.ui.presenter.AbsWithdrawConditionPresenter;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.merchant.entities.WDMaintenance;

import static vn.com.vng.zalopay.paymentapps.PaymentAppConfig.getAppResource;

/**
 * Created by khattn on 3/10/17.
 * Search Category Presenter
 */

public class SearchCategoryPresenter extends AbsWithdrawConditionPresenter<ISearchCategoryView> {

    private final MerchantStore.Repository mMerchantRepository;
    private AppResourceStore.Repository mAppResourceRepository;
    private FriendStore.Repository mFriendRepository;
    private Navigator mNavigator;
    private Context mContext;

    private List<InsideApp> mListApp;
    private List<ZaloFriend> mListFriend;

    private List<InsideApp> mResultListApp;
    private List<ZaloFriend> mResultListFriend;

    @Inject
    SearchCategoryPresenter(Context context, User user,
                            MerchantStore.Repository mMerchantRepository,
                            AppResourceStore.Repository appResourceRepository,
                            FriendStore.Repository friendRepository,
                            Navigator navigator) {
        super(user);
        this.mMerchantRepository = mMerchantRepository;
        this.mAppResourceRepository = appResourceRepository;
        this.mFriendRepository = friendRepository;
        this.mNavigator = navigator;
        this.mContext = context;
    }

    void getListAppResource() {
        Subscription subscription = mAppResourceRepository.getListAppHome()
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

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Get application resource error");
        }
    }

    private void onGetAppResourceSuccess(List<AppResource> resources) {
        Timber.d("get app resource success - size [%s]", resources.size());

        if (mView == null) {
            return;
        }

        AppResource showshow = getAppResource(PaymentAppConfig.Constants.SHOW_SHOW);
        boolean isEnableShowShow = resources.contains(showshow);
        if (isEnableShowShow) {
            resources.remove(showshow);
            resources.add(new AppResource(showshow.appid, showshow.appType, showshow.appname,
                    mView.getContext().getResources().getString(R.string.nav_showshow),
                    AndroidUtils.getColorFromResource(R.color.menu_font_ic_red)));
        }

        mListApp = new ArrayList<>();
        List<InsideApp> insideAppList = InsideAppUtil.getInsideApps();
        mListApp.addAll(insideAppList);
        mListApp.addAll(Lists.transform(resources, InsideAppUtil::transform));
        mView.refreshInsideApps(mListApp);
    }

    void getFriendList() {
        Subscription subscription = mFriendRepository.getZaloFriendList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FriendListSubscriber());

        mSubscription.add(subscription);
    }

    private class FriendListSubscriber extends DefaultSubscriber<List<ZaloFriend>> {
        @Override
        public void onCompleted() {
            Timber.d("onCompleted");
        }

        @Override
        public void onNext(List<ZaloFriend> zaloFriendList) {
            onGetFriendListSuccess(zaloFriendList);
        }

        @Override
        public void onError(Throwable e) {
            Timber.d(e, "Get friend zalo error");
            if (ResponseHelper.shouldIgnoreError(e)) {
                return;
            }

            if (mView != null) {
                mView.showError(ErrorMessageFactory.create(mContext, e));
            }
        }
    }

    private void onGetFriendListSuccess(List<ZaloFriend> zaloFriendList) {
        if (mView == null || zaloFriendList == null) {
            return;
        }

        mListFriend = new ArrayList<>();
        mListFriend.addAll(zaloFriendList);
    }

    void filter(SearchListAppAdapter appAdapter, SearchListFriendAdapter friendAdapter, String text) {
        mResultListApp = new ArrayList<>();
        mResultListFriend = new ArrayList<>();
        text = text.toLowerCase(Locale.getDefault());
        boolean hasData = false;

        if (text.length() == 0) {
            mView.showSearchResultView(false);
            mView.showNoResultView(false);
            return;
        } else {
            int idx;

            for (InsideApp app : mListApp) {
                idx = Strings.getIndexOfSearchString(app.appName, text);
                if(idx != -1) {
                    hasData = true;
                    InsideApp tmp = new InsideApp(app);
                    tmp.appName = changeWordColor(tmp.appName, idx, idx + text.length());
                    mResultListApp.add(tmp);
                }
            }

//            for (ZaloFriend friend : mListFriend) {
//                idx = Strings.getIndexOfSearchString(friend.displayName, text);
//                if(idx != -1) {
//                    hasData = true;
//                    ZaloFriend tmp = new ZaloFriend();
//                    tmp.displayName = friend.displayName;
//                    tmp.avatar = friend.avatar;
//                    tmp.status = friend.status;
//                    tmp.displayName = changeWordColor(tmp.displayName, idx, idx + text.length());
//                    mResultListFriend.add(tmp);
//                }
//            }
        }

        Timber.d("search result - app list size: %s, friend list size: %s", mResultListApp.size(), mResultListFriend.size());
//        setDataCheckLoadMore(appAdapter, friendAdapter);
        appAdapter.setData(mResultListApp);

        if (!hasData) {
            mView.showSearchResultView(false);
            mView.showNoResultView(true);
        } else {
            mView.showNoResultView(false);
            mView.showSearchResultView(true);
        }
    }

    private String changeWordColor(String text, int startIdx, int endIdx) {
        return (text.substring(0, startIdx)
                + "<font color='#9cdaff'>" + text.substring(startIdx, endIdx) + "</font>"
                + text.substring(endIdx));
    }

    private void setDataCheckLoadMore(SearchListAppAdapter appAdapter, SearchListFriendAdapter friendAdapter) {
        if(mResultListApp.size() > 3) {
            appAdapter.setLoadMore(true);
            appAdapter.setData(mResultListApp.subList(0, 3));
        } else {
            appAdapter.setLoadMore(false);
            appAdapter.setData(mResultListApp);
        }

//        if(mResultListFriend.size() > 3) {
//            friendAdapter.setLoadMore(true);
//            friendAdapter.setData(mResultListFriend.subList(0, 3));
//        } else {
//            friendAdapter.setLoadMore(false);
//            friendAdapter.setData(mResultListFriend);
//        }
    }

    void handleLaunchApp(InsideApp app) {
        Timber.d("onclick app %s %s %s %s", app.appType, app.appId, app.insideAppId, app.appName);
        if (app.appType == PaymentAppTypeEnum.REACT_NATIVE.getValue()) {
            if (app.appId == PaymentAppConfig.Constants.RED_PACKET) {
                mNavigator.startMiniAppActivity(mView.getActivity(), ModuleName.RED_PACKET);
            } else {
                AppResource appResource = getAppResource(app.appId);
                if (appResource == null) {
                    appResource = new AppResource(app.appId);
                }
                startPaymentApp(appResource);
            }
        } else if (app.appType == PaymentAppTypeEnum.WEBVIEW.getValue()) {
            startServiceWebViewActivity(app.appId, app.webUrl);
        } else if (app.appType == PaymentAppTypeEnum.INTERNAL_APP.getValue()) {
            if (app.appId == 0) {
                if (app.insideAppId == InsideApp.Constants.LINK_CARD) {
                    mNavigator.startLinkCardActivity(mView.getContext());
                } else if (app.insideAppId == InsideApp.Constants.RECHARGE) {
                    mNavigator.startDepositActivity(mView.getContext());
                } else if (app.insideAppId == InsideApp.Constants.WITHDRAW) {
                    startWithdrawActivity();
                } else if (app.insideAppId == InsideApp.Constants.PAY_QR_CODE) {
                    mNavigator.startScanToPayActivity(mView.getActivity());
                } else if (app.insideAppId == InsideApp.Constants.PROFILE) {
                    mNavigator.startProfileInfoActivity(mView.getActivity());
                }
            } else if (app.appId == PaymentAppConfig.Constants.TRANSFER_MONEY) {
                mNavigator.startTransferMoneyActivity(mView.getActivity());
            } else if (app.appId == PaymentAppConfig.Constants.RECEIVE_MONEY) {
                mNavigator.startReceiveMoneyActivity(mView.getContext());
            }
        } else if (app.appType == PaymentAppTypeEnum.INTERNAL_REACT_NATIVE.getValue()) {
            if (app.insideAppId == InsideApp.Constants.SUPPORT_CENTER) {
                mNavigator.startMiniAppActivity(mView.getActivity(), app.moduleName);
            } else if (app.insideAppId == InsideApp.Constants.NOTIFICATION) {
                mNavigator.startMiniAppActivity(getActivity(), app.moduleName);
            } else if (app.insideAppId == InsideApp.Constants.INFORMATION) {
                mNavigator.startMiniAppActivity(mView.getActivity(), app.moduleName);
            } else if (app.insideAppId == InsideApp.Constants.HISTORY) {
                mNavigator.startTransactionHistoryList(mView.getContext());
            }
        }
    }

    void handleClickSeeMore(SearchListAppAdapter searchListAppAdapter) {
        searchListAppAdapter.setLoadMore(false);
        searchListAppAdapter.setData(mResultListApp);
    }

    void handleClickSeeMore(SearchListFriendAdapter searchListFriendAdapter) {
        searchListFriendAdapter.setLoadMore(false);
        searchListFriendAdapter.setData(mResultListFriend);
    }

    private void startServiceWebViewActivity(long appId, String webViewUrl) {
        Subscription subscription = mMerchantRepository.getMerchantUserInfo(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MerchantUserInfoSubscribe(mNavigator, mView.getActivity(), appId, webViewUrl));
        mSubscription.add(subscription);
    }

    private void startPaymentApp(AppResource app) {
        Subscription subscription = mAppResourceRepository.existResource(app.appid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new StartPaymentAppSubscriber(mNavigator, mView.getActivity(), app));
        mSubscription.add(subscription);
    }

    private boolean isMaintainWithdraw() {
        WDMaintenance wdMaintenance = CShareDataWrapper.getWithdrawMaintenance();
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
        if (!isValidProfile()) {
            mNavigator.startWithdrawConditionActivity(mView.getContext());
        } else {
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
                                public void onOKevent() {
                                    startWithdrawActivity();
                                }
                            });
                }
            });
        }
    }

    @Override
    public Activity getActivity() {
        if (mView == null) {
            return null;
        }
        return mView.getActivity();
    }
}
