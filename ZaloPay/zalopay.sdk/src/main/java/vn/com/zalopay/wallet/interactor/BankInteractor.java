package vn.com.zalopay.wallet.interactor;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.cache.MemoryCache;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.api.RetryWithDelay;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.atm.BankConfig;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.business.entity.atm.BankFunction;
import vn.com.zalopay.wallet.business.entity.gatewayinfo.MiniPmcTransType;
import vn.com.zalopay.wallet.constants.BankFunctionCode;
import vn.com.zalopay.wallet.constants.BankStatus;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.Constants;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;
import vn.com.zalopay.wallet.repository.bank.BankStore;

import static vn.com.zalopay.wallet.constants.Constants.BITMAP_EXTENSION;
import static vn.com.zalopay.wallet.constants.Constants.UNDERLINE;

/**
 * Interactor decide which get data from
 * do some bussiness logic on return result and delegate the result to caller
 * Created by chucvv on 6/8/17.
 */

public class BankInteractor implements IBank {
    private final BankStore.LocalStorage mLocalStorage;
    private final BankStore.BankListService mBankListService;
    private final MemoryCache mMemoryCache;

    public BankInteractor(BankStore.LocalStorage localStorage,
                          BankStore.BankListService bankListService,
                          MemoryCache memoryCache) {
        this.mLocalStorage = localStorage;
        this.mBankListService = bankListService;
        this.mMemoryCache = memoryCache;
        Timber.d("call constructor BankInteractor");
    }

    @Override
    public void clearCheckSum() {
        this.mLocalStorage.clearCheckSum();
    }

    @Override
    public void clearConfig() {
        this.mLocalStorage.clearConfig();
    }

    @Override
    public void resetExpireTime() {
        this.mLocalStorage.setExpireTime(0);
    }

    @Override
    public void setPaymentBank(String userId, String cardKey) {
        this.mLocalStorage.sharePref().setString(cacheKeyPayment(userId), cardKey);
    }

    @Override
    public String getPaymentBank(String userId) {
        String lastBankCode = null;
        try {
            lastBankCode = this.mLocalStorage.sharePref().getString(cacheKeyPayment(userId));
        } catch (Exception e) {
            Timber.w(e);
        }
        return lastBankCode;
    }

    @Override
    public Map<String, String> getBankPrefix() {
        return this.mLocalStorage.getBankPrefix();
    }

    @Override
    public BankConfig getBankConfig(String bankCode) {
        return mLocalStorage.getBankConfig(bankCode);
    }

    @Override
    public Observable<List<BankConfig>> getWithdrawBanks(String appVersion, long currentTime) {
        return getBankList(appVersion, currentTime)
                .flatMap(this::convertToListBankConfigObservable)
                .doOnError(throwable -> Timber.d(throwable != null ? throwable.getMessage() : "Exception"));
    }

    /***
     * reload bank list
     * and return support card list
     * @param appVersion
     * @param currentTime
     * @return
     */
    @Override
    public Observable<List<ZPBank>> getSupportBanks(String appVersion, long currentTime) {
        return getBankList(appVersion, currentTime)
                .flatMap(a -> supportBanks(appVersion))
                .doOnError(throwable -> Timber.d(throwable != null ? throwable.getMessage() : "Exception"));
    }

    private Observable<List<ZPBank>> supportBanks(String appVersion) {
        Timber.d("start load support banks");
        try {
            List<ZPBank> supportBank = new ArrayList<>();
            //cc hardcode
            String bankCodeVisa = CardType.VISA;
            String bankCodeMaster = CardType.MASTER;

            ZPBank visa = prepareBankFromConfig(appVersion, BuildConfig.CC_CODE, false);
            if (visa != null) {
                visa.bankLogo = String.format("%s%s", GlobalData.getStringResource(RS.string.sdk_banklogo_visa), BITMAP_EXTENSION);
                visa.bankCode = bankCodeVisa;
                visa.bankName = GlobalData.getStringResource(RS.string.zpw_string_bankname_visa);
            }

            ZPBank masterCard = prepareBankFromConfig(appVersion, BuildConfig.CC_CODE, false);
            if (masterCard != null) {
                masterCard.bankLogo = supportBankLogo(bankCodeMaster);
                masterCard.bankCode = bankCodeMaster;
                masterCard.bankName = GlobalData.getStringResource(RS.string.zpw_string_bankname_master);
            }
            //build support cards
            String bankCodes = getBankCodeList();
            if (TextUtils.isEmpty(bankCodes)) {
                return Observable.just(supportBank);
            }

            String[] arrayBankCode = bankCodes.split(Constants.COMMA);
            for (String bankCode : arrayBankCode) {
                if (TextUtils.isEmpty(bankCode)) {
                    continue;
                }
                if (BuildConfig.CC_CODE.equals(bankCode)) {
                    supportBank.add(visa);
                    supportBank.add(masterCard);
                    continue;
                }
                boolean isBankAccount = BankAccountHelper.isBankAccount(bankCode);
                ZPBank zpBank = prepareBankFromConfig(appVersion, bankCode, isBankAccount);
                if (zpBank == null) {
                    continue;
                }
                zpBank.bankLogo = supportBankLogo(bankCode);
                zpBank.isBankAccount = isBankAccount;
                if (!supportBank.contains(zpBank)) {
                    supportBank.add(zpBank);
                }
            }
            return Observable.just(supportBank);
        } catch (Exception e) {
            Log.e(this, e);
            return Observable.error(e);
        }
    }

    private Observable<BankConfigResponse> fetchCloud(String platform, String checksum, String appversion) {
        return mBankListService.fetch(platform, checksum, appversion)
                .retryWhen(new RetryWithDelay(Constants.API_MAX_RETRY, Constants.API_DELAY_RETRY))
                .doOnNext(bankConfigResponse -> mLocalStorage.put(bankConfigResponse));
    }

    private ZPBank prepareBankFromConfig(String appVersion, String bankCode, boolean isBankAccount) {
        if (TextUtils.isEmpty(bankCode)) {
            return null;
        }
        //get bank status and message in maintenance or need up version for link transtype
        @BankFunctionCode int bankFunctionCode = isBankAccount ? BankFunctionCode.LINK_BANK_ACCOUNT : BankFunctionCode.LINK_CARD;
        BankConfig bankConfig = this.mLocalStorage.getBankConfig(bankCode);
        if (bankConfig == null) {
            return null;
        }
        ZPBank bank = new ZPBank(bankCode);
        bank.bankName = bankConfig.getDisplayName();
        bank.setBankStatus(bankConfig.status);
        if (bank.bankStatus == BankStatus.ACTIVE) {
            //continue with status in bank function
            BankFunction bankFunction = bankConfig.getBankFunction(bankFunctionCode);
            bank.setBankStatus(BankStatus.DISABLE);
            if (bankFunction != null) {
                bank.setBankStatus(bankFunction.status);
            }
        }
        switch (bank.bankStatus) {
            case BankStatus.DISABLE:
                return null;
            case BankStatus.MAINTENANCE:
                //set maintenance message
                bank.bankMessage = bankConfig.getMaintenanceMessage(bankFunctionCode);
                break;
        }
        if (bank.bankStatus != BankStatus.ACTIVE) {
            return bank;
        }
        //continue check bank future version
        MiniPmcTransType pmcTransType = SDKApplication
                .getApplicationComponent()
                .appInfoInteractor()
                .getPmcTranstype(BuildConfig.ZALOAPP_ID, TransactionType.LINK, isBankAccount, bankCode);
        if (pmcTransType != null && !pmcTransType.isVersionSupport(appVersion)) {
            String message = GlobalData.getStringResource(RS.string.sdk_warning_version_support_linkchannel);
            message = String.format(message, bankConfig.getShortBankName());
            bank.setBankStatus(BankStatus.UPVERSION);
            bank.bankMessage = message;
        }
        return bank;
    }

    private String supportBankLogo(String pBankCode) {
        return String.format("%s%s", pBankCode, BITMAP_EXTENSION);
    }

    @Override
    public Observable<BankConfigResponse> getBankList(String appVersion, long currentTime) {
        String checksum = mLocalStorage.getCheckSum();
        String platform = BuildConfig.PAYMENT_PLATFORM;

        Observable<BankConfigResponse> memoryCache = mMemoryCache.getObservable("SdkBankList")
                .map(object -> {
                    if (object.equals(MemoryCache.EmptyObject)) {
                        return null;
                    } else if (object instanceof BankConfigResponse) {
                        return (BankConfigResponse) object;
                    } else {
                        return null;
                    }
                });
        Observable<BankConfigResponse> bankListCache = mLocalStorage
                .get()
                .subscribeOn(Schedulers.io())
                .onErrorReturn(null);
        Observable<BankConfigResponse> bankListCloud = fetchCloud(platform, checksum, appVersion)
                .flatMap(this::convertToBankConfigResponseObservable);
        return Observable.concat(memoryCache, bankListCache, bankListCloud)
                .first(bankConfigResponse -> bankConfigResponse != null && (bankConfigResponse.expiredtime > currentTime));
    }

    @Override
    public String getBankCodeList() {
        return mLocalStorage.getBankCodeList();
    }

    private String cacheKeyPayment(String userId) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(userId)
                .append(UNDERLINE)
                .append("last_payment");
        return keyBuilder.toString();
    }

    @NonNull
    private Observable<BankConfigResponse> convertToBankConfigResponseObservable(BankConfigResponse bankConfigResponse) {
        if (bankConfigResponse == null) {
            return Observable.error(new RequestException(RequestException.NULL, null));
        } else if (bankConfigResponse.returncode == 1) {
            bankConfigResponse.expiredtime = mLocalStorage.getExpireTime();
            if (bankConfigResponse.bankcardprefixmap == null) {
                bankConfigResponse.bankcardprefixmap = getBankPrefix();
            }
            return Observable.just(bankConfigResponse);
        } else {
            return Observable.error(new RequestException(bankConfigResponse.returncode, bankConfigResponse.returnmessage));
        }
    }

    @NonNull
    private Observable<List<BankConfig>> convertToListBankConfigObservable(BankConfigResponse bankConfigResponse) {
        Timber.d("start load withdraw banks");
        try {
            List<BankConfig> withDrawBanks = new ArrayList<>();
            String bankCodes = getBankCodeList();
            if (TextUtils.isEmpty(bankCodes)) {
                return Observable.just(withDrawBanks);
            }

            String[] arrayBankCode = bankCodes.split(Constants.COMMA);
            for (String bankCode : arrayBankCode) {
                BankConfig bankConfig = mLocalStorage.getBankConfig(bankCode);
                bankConfig.bankLogo = withDrawBankLogo(bankCode);
                if (bankConfig.isWithDrawAllow() && !withDrawBanks.contains(bankConfig)) {
                    withDrawBanks.add(bankConfig);
                }
            }
            return Observable.just(withDrawBanks);
        } catch (Exception e) {
            Log.e(this, e);
            return Observable.error(e);
        }
    }

    private String withDrawBankLogo(String pBankCode) {
        return String.format("bank_%s%s", pBankCode, BITMAP_EXTENSION);
    }
}

