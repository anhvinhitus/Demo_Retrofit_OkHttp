package vn.com.vng.zalopay.zpc.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.data.zpc.ZPCConfig;
import vn.com.vng.zalopay.data.zpc.ZPCStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.zpc.listener.OnFavoriteListener;
import vn.com.vng.zalopay.zpc.model.ZpcViewType;
import vn.com.vng.zalopay.zpc.ui.view.IZaloFriendListView;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public final class ZaloPayContactListPresenter extends AbstractPresenter<IZaloFriendListView> implements OnFavoriteListener {

    private static final int MAX_FAVORITE = 10;
    private static final int TIME_DELAY_SEARCH = 300;

    private final ZPCStore.Repository mZPCRepository;
    protected final Context mContext;
    protected final Navigator mNavigator;

    @ZpcViewType
    private int mViewType = ZpcViewType.ZPC_All;

    private final PublishSubject<String> mDelaySubject;

    @Inject
    ZaloPayContactListPresenter(Context context,
                                Navigator navigator,
                                ZPCStore.Repository zpcRepository) {
        this.mZPCRepository = zpcRepository;
        this.mContext = context;
        this.mNavigator = navigator;
        mDelaySubject = PublishSubject.create();
    }

    @Override
    public void attachView(IZaloFriendListView view) {
        super.attachView(view);
        initSearchPhoneContact();
    }

    private void initSearchPhoneContact() {
        Subscription subscription = mDelaySubject.debounce(TIME_DELAY_SEARCH, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .filter(PhoneUtil::isMobileNumber)
                .onBackpressureLatest()
                .flatMap(this::searchPhoneSkipError)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetUserInfoByPhoneSubscriber(mView));
        mSubscription.add(subscription);
    }

    private Observable<ZPProfile> searchPhoneSkipError(String s) {
        return mZPCRepository.getUserInfoByPhone(s)
                .onErrorResumeNext(throwable -> {
                    ZPProfile profile = new ZPProfile();
                    profile.phonenumber = s;
                    profile.isDataValid = false;
                    return Observable.just(profile);
                });
    }

    @Override
    public void onRemoveFavorite(FavoriteData favoriteData) {
        if (favoriteData == null) {
            return;
        }

        Subscription subscription =
                mZPCRepository.removeFavorite(favoriteData.phoneNumber, favoriteData.zaloId)
                        .doOnError(Timber::d)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);

        if (mView == null) {
            return;
        }
        mView.closeAllSwipeItems();
    }

    @Override
    public void onAddFavorite(FavoriteData favoriteData) {
        if (favoriteData == null) {
            return;
        }

        Subscription subscription =
                mZPCRepository.addFavorite(favoriteData.phoneNumber, favoriteData.zaloId)
                        .doOnError(Timber::d)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    @Override
    public void onMaximumFavorite() {
        if (mView == null) {
            return;
        }

        mView.showNotificationDialog();
    }

    @Override
    public void onSelectFavorite(FavoriteData favoriteData) {
        if (favoriteData == null) {
            return;
        }

        onSelectContactItem(getFragment(), favoriteData);
    }

    public Fragment getFragment() {
        if (mView == null) {
            return null;
        }
        return mView.getFragment();
    }

    private boolean isPhoneBook() {
        return mViewType == ZpcViewType.ZPC_PhoneBook;
    }

    public void refreshFriendList() {
        Subscription subscription = mZPCRepository.fetchZaloFriendFullInfo()
                .flatMap(aBoolean -> mZPCRepository.getZaloFriendsCursor(isPhoneBook()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ContactListSubscriber(false, mView));

        mSubscription.add(subscription);
    }

    private boolean isEnableSyncContact() {
        return ZPCConfig.sEnableSyncContact;
    }

    public void initialize(@Nullable String keySearch, @ZpcViewType int viewType) {
        mViewType = viewType;
        if (!TextUtils.isEmpty(keySearch)) {
            doSearch(keySearch);
        } else {
            getFriendList();
        }
        getFavorite(MAX_FAVORITE);
        initView();
    }

    private void initView() {
        if (mView == null) {
            return;
        }
        mView.setMaxFavorite(MAX_FAVORITE);

        if (isEnableSyncContact()) {
            mView.requestReadContactsPermission();
        }
    }

    private void getFriendList() {
//        SDKApplication.getApplicationComponent().monitorEventTiming().recordEvent(ZPMonitorEvent.TIMING_ZPC_LOAD_START);
        Subscription subscription = mZPCRepository.getZaloFriendsCursor(isPhoneBook())
                .concatWith(retrieveZaloFriendsAsNeeded(isPhoneBook()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ContactListSubscriber(false, mView));

        mSubscription.add(subscription);
    }

    private Observable<Cursor> retrieveZaloFriendsAsNeeded(boolean isTopup) {
        return mZPCRepository.shouldUpdateFriendList()
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> mZPCRepository.fetchZaloFriendFullInfo())
                .flatMap(aBoolean -> mZPCRepository.getZaloFriendsCursor(isTopup))
                ;
    }

    public void syncContact() {
        Subscription subscription = mZPCRepository.syncContact()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    public void doSearch(String s) {
        Subscription subscription = mZPCRepository.findFriends(s, isPhoneBook())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ContactListSubscriber(true, mView));

        mSubscription.add(subscription);
    }

    public void onSelectContactItem(Fragment fragment, Cursor cursor) {
        ZPProfile profile = mZPCRepository.transform(cursor);
        if (profile == null) {
            Timber.d("click contact profile is null");
            return;
        }

        onSelectContactItem(fragment, profile);
    }

    public void onSelectContactItem(Fragment fragment, ZPProfile profile) {
        if (isPhoneBook()) {
            backTopup(fragment, profile);
        } else {
            startTransfer(fragment, profile);
        }
    }

    private void onSelectContactItem(Fragment fragment, FavoriteData favoriteData) {
        if (favoriteData == null) {
            Timber.d("click contact updateFavouriteData data is null");
            return;
        }

        ZPProfile profile = new ZPProfile();
        profile.avatar = favoriteData.avatar;
        profile.displayName = favoriteData.displayName;
        profile.phonenumber = favoriteData.phoneNumber;
        profile.status = favoriteData.status;

        if (isPhoneBook()) {
            backTopup(fragment, profile);
        } else {
            startTransfer(fragment, profile);
        }
    }

    private void backTopup(Fragment fragment, ZPProfile profile) {
        Activity activity = fragment.getActivity();
        Intent data = new Intent();
        data.putExtra("profile", profile);
        activity.setResult(Activity.RESULT_OK, data);
        AndroidUtils.hideKeyboard(activity);
        activity.finish();
    }

    public void startTransfer(Fragment fragment, ZPProfile profile) {
        if (profile.status != 1) {
            Timber.d("user profile [status %s]", profile.status);
            showDialogNotUsingApp(profile);
            return;
        }

        TransferObject object = new TransferObject(profile);
        object.transferMode = Constants.TransferMode.TransferToZaloFriend;
        object.activateSource = Constants.ActivateSource.FromTransferActivity;
        mNavigator.startTransferActivity(fragment, object, Constants.REQUEST_CODE_TRANSFER);
    }

    void showDialogNotUsingApp(ZPProfile zaloProfile) {
        if (mView == null) {
            return;
        }

        String user = zaloProfile.displayName == null || TextUtils.isEmpty(zaloProfile.displayName) ? zaloProfile.phonenumber : zaloProfile.displayName;

        String message = String.format(mContext.getString(R.string.account_not_use_zalopay), user, user);
        DialogHelper.showNotificationDialog((Activity) mView.getContext(),
                message,
                null);
    }

    private void getFavorite(int limitFavorite) {
        Subscription subscription = mZPCRepository.getFavorites(limitFavorite)
                .filter(data -> !Lists.isEmptyOrNull(data))
                .doOnError(Timber::d)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<List<FavoriteData>>() {
                    @Override
                    public void onNext(List<FavoriteData> data) {
                        if (mView != null) {
                            mView.setFavorite(data);
                        }
                    }
                });
        mSubscription.add(subscription);
    }

    public void getUserInfoNotInZPC(String phone) {
        mDelaySubject.onNext(phone);
    }
}
