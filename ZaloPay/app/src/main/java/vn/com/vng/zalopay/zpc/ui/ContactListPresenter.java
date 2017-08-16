package vn.com.vng.zalopay.zpc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.MainThread;
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
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.data.zpc.ZPCConfig;
import vn.com.vng.zalopay.data.zpc.ZPCStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.monitors.ZPMonitorEventTiming;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.zpc.listener.OnFavoriteListener;
import vn.com.vng.zalopay.zpc.model.ZPCPickupMode;
import vn.com.vng.zalopay.zpc.model.ZpcViewType;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public final class ContactListPresenter extends AbstractPresenter<ContactListView> implements OnFavoriteListener {

    private static final int MAX_FAVORITE = 10;
    private static final int TIME_DELAY_SEARCH = 300;

    private final ZPCStore.Repository mZPCRepository;
    protected final Context mContext;

    @ZpcViewType
    private int mViewType = ZpcViewType.ZPC_All;
    private int mPickupMode = ZPCPickupMode.DEFAULT;

    private final PublishSubject<String> mDelaySubject;
    private ZPMonitorEventTiming mEventTiming;

    @Inject
    User mUser;

    @Inject
    ContactListPresenter(Context context,
                         ZPCStore.Repository zpcRepository,
                         ZPMonitorEventTiming eventTiming) {
        this.mZPCRepository = zpcRepository;
        this.mContext = context;
        mDelaySubject = PublishSubject.create();
        this.mEventTiming = eventTiming;
    }

    @Override
    public void attachView(ContactListView view) {
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

    void refreshFriendList() {
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

    public void initialize(@Nullable String keySearch, @ZpcViewType int viewType, int pickupMode) {
        mViewType = viewType;
        mPickupMode = pickupMode;
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

    void syncContact() {
        Subscription subscription = mZPCRepository.syncContact()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    void doSearch(String s) {
        Subscription subscription = mZPCRepository.findFriends(s, isPhoneBook())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ContactListSubscriber(true, mView));

        mSubscription.add(subscription);
    }

    void onSelectContactItem(Fragment fragment, Cursor cursor) {
        ZPProfile profile = mZPCRepository.transform(cursor);
        if (profile == null) {
            Timber.d("click contact profile is null");
            return;
        }

        onSelectContactItem(fragment, profile);
    }

    void onSelectNonContactItem(Fragment fragment, ZPProfile profile) {
        if ((mPickupMode & ZPCPickupMode.ALLOW_NON_CONTACT_ITEM) == 0) {
            return;
        }

        onSelectContactItem(fragment, profile);
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

        onSelectContactItem(fragment, profile);
    }

    private void onSelectContactItem(Fragment fragment, ZPProfile profile) {
        if ((mPickupMode & ZPCPickupMode.ALLOW_NON_ZALOPAY_USER) == 0) {
            // disable non zalo pay user
            if (profile.status != 1) {
                Timber.d("user profile [status %s]", profile.status);
                showDialogNotUsingApp(profile);
                return;
            }
        }

        if ((mPickupMode & ZPCPickupMode.ALLOW_OWN_NUMBER) == 0) {
            // disable own number
            String userPhoneNo = PhoneUtil.formatPhoneNumber(mUser.phonenumber);
            if (TextUtils.isEmpty(userPhoneNo)) {
                Timber.d("can not get user phone number");
                return;
            }

            if (userPhoneNo.equals(profile.phonenumber)) {
                Timber.d("user transfer to him(her)self [user number: %s / transfer number: %s]", userPhoneNo, profile.phonenumber);
                showDialogTransferToSelf();
                return;
            }
        }

        Activity activity = fragment.getActivity();
        Intent data = new Intent();
        data.putExtra("profile", profile);
        activity.setResult(Activity.RESULT_OK, data);
        AndroidUtils.hideKeyboard(activity);
        activity.finish();
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

    void showDialogTransferToSelf() {
        if (mView == null) {
            return;
        }

        DialogHelper.showNotificationDialog((Activity) mView.getContext(),
                mContext.getString(R.string.exception_transfer_for_self),
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

    void getUserInfoNotInZPC(String phone) {
        mDelaySubject.onNext(phone);
    }

    void monitorTimingZPCEnd() {
        if (mEventTiming == null) {
            return;
        }

        mEventTiming.recordEvent(ZPMonitorEvent.TIMING_ZPC_LOAD_END);
    }
}
