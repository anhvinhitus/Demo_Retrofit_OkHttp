package vn.com.zalopay.wallet.helper;

import android.text.TextUtils;

import java.util.List;

import vn.com.zalopay.wallet.business.dao.SharedPreferencesManager;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.base.BaseResponse;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBankAccount;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.DBaseMap;
import vn.com.zalopay.wallet.datasource.request.BaseRequest;
import vn.com.zalopay.wallet.datasource.request.GetBankAccountList;
import vn.com.zalopay.wallet.listener.ICheckExistBankAccountListener;
import vn.com.zalopay.wallet.listener.IGetBankAccountList;
import vn.com.zalopay.wallet.merchant.listener.IReloadMapInfoListener;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;

public class BankAccountHelper {
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
            Log.e("hasBankAccountOnCache", e);
        }
        return false;
    }

    public static void existBankAccount(boolean pReloadList, final ICheckExistBankAccountListener pListener, final String pBankCode) {
        try {
            loadBankAccountList(pReloadList, new IReloadMapInfoListener<DBankAccount>() {
                @Override
                public void onComplete(List<DBankAccount> pMapList) {
                    DBankAccount bankAccount = new DBankAccount();
                    bankAccount.bankcode = pBankCode;

                    if (pMapList != null && pMapList.size() > 0 && pListener != null) {
                        pListener.onCheckExistBankAccountComplete(pMapList.contains(bankAccount));
                    } else if (pListener != null) {
                        pListener.onCheckExistBankAccountComplete(false);
                    } else {
                        Log.e(this, "existBankAccount===pListener=NULL");
                    }
                }

                @Override
                public void onError(String pErrorMess) {
                    if (pListener != null) {
                        pListener.onCheckExistBankAccountFail(pErrorMess);
                    }
                }
            });
        } catch (Exception ex) {
            Log.e("existBankAccount", ex);
            if (pListener != null) {
                pListener.onCheckExistBankAccountFail(null);
            }
        }

    }

    /***
     * reload bank account list
     *
     * @param pReload
     * @param pReloadBankAccountInfoListener
     */
    public static void loadBankAccountList(boolean pReload, final IReloadMapInfoListener pReloadBankAccountInfoListener) {
        try {
            if (pReload) {
                SharedPreferencesManager.getInstance().setBankAccountCheckSum(null);
            }

            BaseRequest getBankAccount = new GetBankAccountList(new IGetBankAccountList() {

                @Override
                public void onGetBankAccountListComplete(BaseResponse pResponse) {
                    if (pResponse instanceof BankAccountListResponse
                            && BankAccountHelper.isNeedUpdateBankAccountInfoOnCache(((BankAccountListResponse) pResponse).bankaccountchecksum)) {
                        try {
                            BankAccountHelper.updateBankAccountListOnCache(((BankAccountListResponse) pResponse).bankaccountchecksum, ((BankAccountListResponse) pResponse).bankaccounts);
                        } catch (Exception e) {
                            Log.e(this, e);
                        }

                        if (pReloadBankAccountInfoListener != null) {
                            pReloadBankAccountInfoListener.onComplete(((BankAccountListResponse) pResponse).bankaccounts);
                        }
                    } else if (pResponse != null && pResponse.returncode == 1 && pReloadBankAccountInfoListener != null) {
                        pReloadBankAccountInfoListener.onComplete(null);

//                        // hard code to test linkAcc
//                        List<DBankAccount> bankaccounts = new ArrayList<DBankAccount>();
//                        DBankAccount bankAccount = new DBankAccount();
//                        bankAccount.bankcode = "ZPVCB";
//                        bankAccount.firstaccountno = "042100";
//                        bankAccount.lastaccountno = "6723";
//                        bankaccounts.add(bankAccount);
//                        pReloadMapCardInfoListener.onComplete(bankaccounts);
                    } else if (pReloadBankAccountInfoListener != null) {
                        pReloadBankAccountInfoListener.onError(null);
                    }
                }
            });

            getBankAccount.makeRequest();

        } catch (Exception e) {
            if (pReloadBankAccountInfoListener != null) {
                pReloadBankAccountInfoListener.onError(null);
            }
            Log.e("loadMapCardList", e);
        }
    }

    public static boolean isNeedUpdateBankAccountInfoOnCache(String pBankAccountCheckSum) {
        try {
            String bankAccountCheckSumOnCache = SharedPreferencesManager.getInstance().getBankAccountCheckSum();
            if (TextUtils.isEmpty(bankAccountCheckSumOnCache) || (!TextUtils.isEmpty(bankAccountCheckSumOnCache) && !bankAccountCheckSumOnCache.equals(pBankAccountCheckSum))) {
                return true;
            }
        } catch (Exception e) {
            Log.e("isNeedUpdateBankAccountInfoOnCache", e);
        }

        return false;
    }

    public static void updateBankAccountListOnCache(String pInfoCheckSum, List<DBankAccount> pMapList) throws Exception {
        //update checksum
        SharedPreferencesManager.getInstance().setBankAccountCheckSum(pInfoCheckSum);
        Log.d("updateBankAccountListOnCache", "===pInfoCheckSum=" + pInfoCheckSum);
        Log.d("updateBankAccountListOnCache", "===pMapList=" + GsonUtils.toJsonString(pMapList));
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
            Log.d("updateBankAccountListOnCache", "====mapp account ID===" + mappedCardID.toString());
        } else {
            //clear back account list
            SharedPreferencesManager.getInstance().resetBankListOnCache(GlobalData.getPaymentInfo().userInfo.zaloPayUserId);
            Log.d("updateBankAccountListOnCache", "===clearing bank account===");
        }
    }
}
