package vn.com.vng.zalopay.zpc.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.data.util.PhoneUtil;
import vn.com.vng.zalopay.data.zpc.ZPCConfig;
import vn.com.vng.zalopay.data.zpc.ZPCStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.domain.model.FavoriteData;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.domain.model.ZPCGetByPhone;
import vn.com.vng.zalopay.domain.model.ZPProfile;
import vn.com.vng.zalopay.monitors.ZPMonitorEvent;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.transfer.model.TransferObject;
import vn.com.vng.zalopay.ui.presenter.AbstractPresenter;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;
import vn.com.vng.zalopay.zpc.listener.OnFavoriteListener;
import vn.com.vng.zalopay.zpc.model.ZpcViewType;
import vn.com.vng.zalopay.zpc.ui.view.IZaloFriendListView;
import vn.com.zalopay.wallet.controller.SDKApplication;

/**
 * Created by AnhHieu on 10/10/16.
 * *
 */

public final class ZaloPayContactListPresenter extends AbstractPresenter<IZaloFriendListView> implements OnFavoriteListener {

    private static final int MAX_FAVORITE = 10;

    private final ZPCStore.Repository mFriendRepository;
    protected final Context mContext;
    protected final Navigator mNavigator;

    @ZpcViewType
    private int mViewType = ZpcViewType.ZPC_All;

    @Inject
    User user;

    @Inject
    ZaloPayContactListPresenter(Context context, Navigator navigator, ZPCStore.Repository friendRepository) {
        this.mFriendRepository = friendRepository;
        this.mContext = context;
        this.mNavigator = navigator;
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
        Subscription subscription = mFriendRepository.fetchZaloFriendFullInfo()
                .flatMap(aBoolean -> mFriendRepository.getZaloFriendsCursor(isPhoneBook()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ContactListSubscriber(false, mView));

        mSubscription.add(subscription);
    }

    private boolean isEnableSyncContact() {
        return ZPCConfig.sEnableSyncContact;
    }

    public void initialize(@Nullable String keySearch, @ZpcViewType int viewType, ListView listView) {
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
        Subscription subscription = mFriendRepository.getZaloFriendsCursor(isPhoneBook())
                .concatWith(retrieveZaloFriendsAsNeeded(isPhoneBook()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ContactListSubscriber(false, mView));

        mSubscription.add(subscription);
    }

    private Observable<Cursor> retrieveZaloFriendsAsNeeded(boolean isTopup) {
        return mFriendRepository.shouldUpdateFriendList()
                .filter(Boolean::booleanValue)
                .flatMap(aBoolean -> mFriendRepository.fetchZaloFriendFullInfo())
                .flatMap(aBoolean -> mFriendRepository.getZaloFriendsCursor(isTopup))
                ;
    }

    public void syncContact() {
        Subscription subscription = mFriendRepository.syncContact()
                .subscribeOn(Schedulers.io())
                .subscribe(new DefaultSubscriber<>());
        mSubscription.add(subscription);
    }

    public void doSearch(String s) {
        Subscription subscription = mFriendRepository.findFriends(s, isPhoneBook())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ContactListSubscriber(true, mView));

        mSubscription.add(subscription);
    }

    public void onSelectContactItem(Fragment fragment, Cursor cursor) {
        ZPProfile profile = mFriendRepository.transform(cursor);
        if (profile == null) {
            Timber.d("click contact profile is null");
            return;
        }

        if (isPhoneBook()) {
            backTopup(fragment, profile);
        } else {
            startTransfer(profile);
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
            startTransfer(profile);
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

    private void startTransfer(ZPProfile profile) {
        Subscription subscription = mFriendRepository.getUserInfoByPhone(user.zaloPayId, user.accesstoken, profile.phonenumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new GetUserByPhoneSubscriber(this, profile));

        mSubscription.add(subscription);
//        if (profile.status != 1) {
//            Timber.d("user profile [status %s]", profile.status);
//            showDialogNotUsingApp(profile);
//            return;
//        }
//
//        TransferObject object = new TransferObject(profile);
//        object.transferMode = Constants.TransferMode.TransferToZaloFriend;
//        object.activateSource = Constants.ActivateSource.FromTransferActivity;
//        mNavigator.startTransferActivity(fragment, object, Constants.REQUEST_CODE_TRANSFER);
    }

    void startTransfer(TransferObject transferObject){
        if(mNavigator == null){
            return;
        }
        if (mView == null) {
            return;
        }
        mNavigator.startTransferActivity((Fragment) mView, transferObject, Constants.REQUEST_CODE_TRANSFER);
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
        Subscription subscription = mFriendRepository.getFavorites(limitFavorite)
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

    public void handleNumberNotInZPC(String phoneNumber) {
        if (getFragment() == null) {
            return;
        }

        if (!PhoneUtil.isMobileNumber(phoneNumber)) {
            return;
        }

        FavoriteData favoriteData = new FavoriteData();
        favoriteData.displayName = "";
        favoriteData.phoneNumber = phoneNumber;
        onSelectContactItem(getFragment(), favoriteData);
    }

    @Override
    public void onRemoveFavorite(FavoriteData favoriteData) {
        if (favoriteData == null) {
            return;
        }

        Subscription subscription =
                mFriendRepository.removeFavorite(favoriteData.phoneNumber, favoriteData.zaloId)
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
                mFriendRepository.addFavorite(favoriteData.phoneNumber, favoriteData.zaloId)
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

    static class GetUserByPhoneSubscriber extends DefaultSubscriber<ZPCGetByPhone> {
        private WeakReference<ZaloPayContactListPresenter> mPresenter;
        private ZPProfile mProfile;

        GetUserByPhoneSubscriber(ZaloPayContactListPresenter presenter, ZPProfile profile) {
            mPresenter = new WeakReference<>(presenter);
            mProfile = profile;
        }

        @Override
        public void onNext(ZPCGetByPhone zpcGetByPhone) {
            if (zpcGetByPhone == null) {
                return;
            }
            ZaloPayContactListPresenter presenter = mPresenter.get();
            if(presenter == null){
                return;
            }

            if (zpcGetByPhone.returnCode == 1) {
                TransferObject object = new TransferObject(zpcGetByPhone);
                object.transferMode = Constants.TransferMode.TransferToZaloFriend;
                object.activateSource = Constants.ActivateSource.FromTransferActivity;
                presenter.startTransfer(object);
            } else {
                Timber.d("user get by phone [error :  %s]", zpcGetByPhone.returnMessage == null ? "" : zpcGetByPhone.returnMessage);
                presenter.showDialogNotUsingApp(mProfile);
            }
        }
    }
}
