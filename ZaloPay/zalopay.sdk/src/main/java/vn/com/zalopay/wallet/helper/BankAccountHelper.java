package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import java.util.List;

import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.datasource.task.MapBankAccountListTask;
import vn.com.zalopay.wallet.listener.ICheckExistBankAccountListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class BankAccountHelper {
    private static final String TAG = BankAccountHelper.class.getCanonicalName();

    public static boolean isBankAccount(String pBankCode) {
        return !TextUtils.isEmpty(pBankCode) && pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank));
    }

    public static boolean hasBankAccountOnCache(String pUserId, String pBankCode) {
        try {
            List<DBankAccount> bankAccountList = SharedPreferencesManager.getInstance().getBankAccountList(pUserId);

            DBankAccount bankAccount = new DBankAccount();
            bankAccount.bankcode = pBankCode;

            boolean existedBankAccount = bankAccountList != null && bankAccountList.size() > 0 && bankAccountList.contains(bankAccount);
            return existedBankAccount;

        } catch (Exception e) {
            Log.e(TAG, e);
        }
        return false;
    }

    public static void existBankAccount(boolean pReloadList, final ICheckExistBankAccountListener pListener, final String pBankCode) {
        try {
            loadBankAccountList(pReloadList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<BaseResponse>() {
                        @Override
                        public void onSuccess(BaseResponse response) {
                            DBankAccount bankAccount = new DBankAccount();
                            bankAccount.bankcode = pBankCode;
                            if (response instanceof BankAccountListResponse) {
                                pListener.onCheckExistBankAccountComplete(((BankAccountListResponse) response).bankaccounts.contains(bankAccount));
                            } else if (pListener != null) {
                                pListener.onCheckExistBankAccountComplete(false);
                            } else {
                                Log.e(this, "pListener = NULL");
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            if (pListener != null) {
                                pListener.onCheckExistBankAccountFail(null);
                            }
                        }
                    });
        } catch (Exception ex) {
            Log.e(TAG, ex);
            if (pListener != null) {
                pListener.onCheckExistBankAccountFail(null);
            }
        }

    }

    /***
     * reload bank account list
     * @param pReload
     */
    public static Single<BaseResponse> loadBankAccountList(boolean pReload) {
        return Single.create(subscriber -> {
            try {
                if (pReload) {
                    SharedPreferencesManager.getInstance().setBankAccountCheckSum(null);
                }
                BaseTask getBankAccount = new MapBankAccountListTask(pResponse -> {
                    subscriber.onSuccess(pResponse);
                });
                getBankAccount.makeRequest();

            } catch (Exception e) {
                Log.e(Log.TAG, e);
                subscriber.onError(e);
            }
        });
    }

    public static boolean needUpdateMapBankAccountListOnCache(String pBankAccountCheckSum) {
        try {
            String bankAccountCheckSumOnCache = SharedPreferencesManager.getInstance().getBankAccountCheckSum();
            if (TextUtils.isEmpty(bankAccountCheckSumOnCache) || (!TextUtils.isEmpty(bankAccountCheckSumOnCache) && !bankAccountCheckSumOnCache.equals(pBankAccountCheckSum))) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        return false;
    }

    public static void saveMapBankAccountListToCache(String pInfoCheckSum, List<DBankAccount> pMapList) throws Exception {
        //update checksum
        SharedPreferencesManager.getInstance().setBankAccountCheckSum(pInfoCheckSum);
        Log.d(TAG, "saved bank account list checksum " + pInfoCheckSum);
        //map card list
        if (pMapList != null && pMapList.size() > 0) {
            StringBuilder mappedCardID = new StringBuilder();
            int count = 0;
            for (DBaseMap mappedCard : pMapList) {
                count++;
                //cache card info
                SharedPreferencesManager.getInstance().setMapCard(mappedCard.getCardKey(), GsonUtils.toJsonString(mappedCard));
                mappedCardID.append(mappedCard.getCardKey());
                if (count < pMapList.size()) {
                    mappedCardID.append(Constants.COMMA);
                }
            }
            //cache map list
            SharedPreferencesManager.getInstance().setBankAccountKeyList(GlobalData.getPaymentInfo().userInfo.zaloPayUserId, mappedCardID.toString());
            Log.d(TAG, "saved map bank account list ids " + mappedCardID.toString());
        } else {
            //clear back account list
            SharedPreferencesManager.getInstance().resetBankListOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
            Log.d(TAG, "cleared map bank account list");
        }
    }
}
