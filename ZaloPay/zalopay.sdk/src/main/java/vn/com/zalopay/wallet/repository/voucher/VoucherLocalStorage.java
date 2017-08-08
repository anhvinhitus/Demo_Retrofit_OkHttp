package vn.com.zalopay.wallet.repository.voucher;

/*
 * Created by chucvv on 8/7/17.
 */

import android.text.TextUtils;

import rx.Observable;
import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.business.entity.voucher.VoucherInfo;
import vn.com.zalopay.wallet.repository.AbstractLocalStorage;
import vn.com.zalopay.wallet.repository.SharedPreferencesManager;

public class VoucherLocalStorage extends AbstractLocalStorage implements VoucherStore.LocalStorage {
    public VoucherLocalStorage(SharedPreferencesManager sharedPreferences) {
        super(sharedPreferences);
    }

    @Override
    public SharedPreferencesManager sharePref() {
        return mSharedPreferences;
    }

    @Override
    public void put(String userId, VoucherInfo voucherInfo) {
        try {
            if (voucherInfo == null || TextUtils.isEmpty(voucherInfo.vouchercode)) {
                return;
            }
            mSharedPreferences.setRevertVoucher(userId, voucherInfo.vouchercode, GsonUtils.toJsonString(voucherInfo));
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public Observable<String> get(String userId) {
        return Observable.defer(() -> {
            try {
                String[] vouchers = mSharedPreferences.getVouchers(userId);
                Timber.d("get vouchers size %s", vouchers != null ? vouchers.length : 0);
                if (vouchers == null || vouchers.length <= 0) {
                    return Observable.just(null);
                }
                return Observable.from(vouchers);
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    @Override
    public Observable<Boolean> clearVoucher(String userId, String voucherCode) {
        return Observable.defer(() -> {
            try {
                mSharedPreferences.clearVouchers(userId, voucherCode);
                Timber.d("clear voucher sign %s", voucherCode);
                return Observable.just(true);
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }
}
