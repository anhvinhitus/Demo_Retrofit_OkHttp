package vn.com.vng.zalopay.zpc.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
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
    private static final long API_RESPONE_LONG_LIFE = 5 * 60 * 1000; // 5 minutes
    private final String PREF_CACHED_NOT_IN_ZPC = "pref_cached_not_in_zpc";

    private final ZPCStore.Repository mFriendRepository;
    protected final Context mContext;
    protected final Navigator mNavigator;
    protected long mStartCachedTime;
    private ZPCGetByPhone mZPCGetByPhone;
    private List<ZPCGetByPhone> mListCachedData;
    private String mCurrentPhoneNumber;
    private final SharedPreferences mPreferences = AndroidApplication.instance().getAppComponent().sharedPreferences();

    @ZpcViewType
    private int mViewType = ZpcViewType.ZPC_All;

    @Inject
    User user;

    @Inject
    ZaloPayContactListPresenter(Context context, Navigator navigator, ZPCStore.Repository friendRepository) {
        this.mFriendRepository = friendRepository;
        this.mContext = context;
        this.mNavigator = navigator;
        this.mStartCachedTime = 0;
        this.mListCachedData = new ArrayList<>();
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

    @Override
    public void pause() {
        if (mListCachedData == null || mListCachedData.size() <= 0) {
            return;
        }

        String jsonDataToCached = getStringJsonFromListCached(mListCachedData);
        cacheData(jsonDataToCached);
        Timber.d("Json string to cached list data not in zpc: %s", jsonDataToCached);
    }

    public Fragment getFragment() {
        if (mView == null) {
            return null;
        }
        return mView.getFragment();
    }

    void showLoadingView() {
        if (mView == null) {
            return;
        }

        mView.showLoading();
    }

    void hideLoadingView() {
        if (mView == null) {
            return;
        }

        mView.hideLoading();
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

        String jsonListCachedData = mPreferences.getString(PREF_CACHED_NOT_IN_ZPC, "");
        if (!TextUtils.isEmpty(jsonListCachedData)) {
            mListCachedData = getListCachedData(jsonListCachedData);
        }
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

    public void onSelectContactItem(Fragment fragment, ZPCGetByPhone zpcGetByPhone, String phoneNumber) {
        if (zpcGetByPhone == null) {
            Timber.d("click contact not in zpc data is null");
            return;
        }

        ZPProfile profile = new ZPProfile();
        if(!TextUtils.isEmpty(zpcGetByPhone.avatar)) {
            profile.avatar = zpcGetByPhone.avatar;
        }
        profile.displayName = TextUtils.isEmpty(zpcGetByPhone.displayName) ? "" : zpcGetByPhone.displayName;
        profile.phonenumber = TextUtils.isEmpty(PhoneUtil.formatPhoneNumber(zpcGetByPhone.phoneNumber)) ?
                phoneNumber : PhoneUtil.formatPhoneNumber(zpcGetByPhone.phoneNumber);
        profile.status = zpcGetByPhone.returnCode;

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

    public void getUserInfoNotInZPC(String number) {
        if (mView == null) {
            return;
        }

        showLoadingView();
        if (mListCachedData != null && mListCachedData.size() > 0) {
            Timber.d("Check the phone input in list cached data");
            for (ZPCGetByPhone zpcGetByPhone : mListCachedData) {
                String phoneNo = PhoneUtil.formatPhoneNumber(zpcGetByPhone.phoneNumber);
                if (number.equals(phoneNo)) {
                    loadProfileNotInZPC(zpcGetByPhone);
                    return;
                }
            }
        }

        if (System.currentTimeMillis() - mStartCachedTime > API_RESPONE_LONG_LIFE ||
                !number.equals(mCurrentPhoneNumber)) {
            mCurrentPhoneNumber = number;
            Timber.d("Access local cached object: over 5 minutes or not in cached data");
            Subscription subscription = mFriendRepository.getUserInfoByPhone(user.zaloPayId, user.accesstoken, number)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new GetUserByPhoneSubscriber(this));

            mSubscription.add(subscription);
        } else {
            if(mZPCGetByPhone.returnCode == 1) {
                loadProfileNotInZPC(mZPCGetByPhone);
            } else {
                loadDefaultNotInZPC(mZPCGetByPhone);
            }
            Timber.d("Access local cached object: cached object still available in 5 minutes");
        }
    }

    private void startTransfer(Fragment fragment, ZPProfile profile) {
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

    void doTransfer(ZPCGetByPhone zpcGetByPhone) {
        if (mNavigator == null) {
            return;
        }

        if (mView == null) {
            return;
        }

        if (zpcGetByPhone == null) {
            Timber.d("Access local cached object: cached object get null");
            return;
        }

        hideLoadingView();
        TransferObject object = new TransferObject(zpcGetByPhone);
        object.transferMode = Constants.TransferMode.TransferToZaloFriend;
        object.activateSource = Constants.ActivateSource.FromTransferActivity;

        mNavigator.startTransferActivity((Fragment) mView, object, Constants.REQUEST_CODE_TRANSFER);
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

    void cacheData(String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_CACHED_NOT_IN_ZPC, value);
        editor.apply();
    }

    String getStringJsonFromListCached(List<ZPCGetByPhone> listCached) {
        return new Gson().toJson(listCached);
    }

    List<ZPCGetByPhone> getListCachedData(String json) {
        List<ZPCGetByPhone> list = new Gson().fromJson(json, new TypeToken<ArrayList<ZPCGetByPhone>>() {
        }.getType());
        return list;
    }

    void loadDefaultNotInZPC(ZPCGetByPhone zpcGetByPhone) {
        if (mView == null) {
            return;
        }

        mView.loadDefaultNotInZPCView(zpcGetByPhone);
    }

    void loadProfileNotInZPC(ZPCGetByPhone zpcGetByPhone) {
        if (mView == null) {
            return;
        }

        mView.updateProfileNotInZPC(zpcGetByPhone);
    }

    static class GetUserByPhoneSubscriber extends DefaultSubscriber<ZPCGetByPhone> {
        private WeakReference<ZaloPayContactListPresenter> mPresenter;

        GetUserByPhoneSubscriber(ZaloPayContactListPresenter presenter) {
            mPresenter = new WeakReference<>(presenter);
        }

        @Override
        public void onNext(ZPCGetByPhone zpcGetByPhone) {
            if (zpcGetByPhone == null) {
                return;
            }

            ZaloPayContactListPresenter presenter = mPresenter.get();
            if (presenter == null) {
                return;
            }

            if (zpcGetByPhone.returnCode == 1) {
                presenter.mStartCachedTime = System.currentTimeMillis();
                presenter.mListCachedData.add(zpcGetByPhone);
                presenter.mZPCGetByPhone = zpcGetByPhone;
                presenter.loadProfileNotInZPC(zpcGetByPhone);
            } else {
                Timber.d("User get by phone [error :  %s]", zpcGetByPhone.returnMessage == null ? "" : zpcGetByPhone.returnMessage);
                presenter.loadDefaultNotInZPC(zpcGetByPhone);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (mPresenter.get() == null) {
                return;
            }

            mPresenter.get().hideLoadingView();
            Timber.d("User get by phone call api [error :  %s]", e.getMessage());
        }
    }
}
