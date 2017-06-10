package vn.com.zalopay.wallet.interactor;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.BuildConfig;
import vn.com.zalopay.wallet.business.data.Constants;
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
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.exception.RequestException;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.helper.SchedulerHelper;
import vn.com.zalopay.wallet.merchant.entities.ZPBank;
import vn.com.zalopay.wallet.repository.banklist.BankListStore;

/**
 * Interactor decide which get data from
 * do some bussiness logic on return result and delegate the result to caller
 * Created by chucvv on 6/8/17.
 */

public class BankListInteractor implements IBankList {
    public BankListStore.Repository mBankListRepository;
    protected Func1<BankConfigResponse, Observable<BankConfigResponse>> mapResult = bankConfigResponse -> {
        if (bankConfigResponse == null) {
            return Observable.error(new RequestException(RequestException.NULL, null));
        } else if (bankConfigResponse.returncode == 1) {
            bankConfigResponse.expiredtime = mBankListRepository.getLocalStorage().getExpireTime();
            if (bankConfigResponse.bankcardprefixmap == null) {
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>() {
                }.getType();
                HashMap<String, String> bankMap = GsonUtils.fromJsonString(mBankListRepository.getLocalStorage().getBankPrefix(), type);
                bankConfigResponse.bankcardprefixmap = bankMap;
            }
            return Observable.just(bankConfigResponse);
        } else {
            return Observable.error(new RequestException(bankConfigResponse.returncode, bankConfigResponse.returnmessage));
        }
    };

    @Inject
    public BankListInteractor(BankListStore.Repository bankListRepository) {
        this.mBankListRepository = bankListRepository;
        Log.d(this, "call constructor BankListInteractor");
    }

    private Func1<BankConfigResponse, Observable<List<ZPBank>>> supportBanks(String appVersion) {
        return new Func1<BankConfigResponse, Observable<List<ZPBank>>>() {
            @Override
            public Observable<List<ZPBank>> call(BankConfigResponse bankConfigResponse) {
                Log.d(this, "start load support banks");
                try {
                    List<ZPBank> supportBank = new ArrayList<>();
                    //cc must be hardcode
                    String bankCodeVisa = CardType.VISA;
                    String bankCodeMaster = CardType.MASTER;
                    ZPBank visa = getCardFromBankConfig(appVersion, BuildConfig.CC_CODE, false);
                    visa.bankLogo = getBankLogo(bankCodeVisa);
                    visa.bankCode = bankCodeVisa;
                    visa.bankName = GlobalData.getStringResource(RS.string.zpw_string_bankname_visa);
                    ;
                    ZPBank masterCard = getCardFromBankConfig(appVersion, BuildConfig.CC_CODE, false);
                    masterCard.bankLogo = getBankLogo(bankCodeMaster);
                    masterCard.bankCode = bankCodeMaster;
                    masterCard.bankName = GlobalData.getStringResource(RS.string.zpw_string_bankname_master);
                    ;

                    //build support cards
                    String bankCodes = mBankListRepository.getLocalStorage().getBankCodeList();
                    if (!TextUtils.isEmpty(bankCodes)) {
                        String[] arrayBankCode = bankCodes.split(Constants.COMMA);
                        for (int i = 0; i < arrayBankCode.length; i++) {
                            String bankCode = arrayBankCode[i];
                            if (!TextUtils.isEmpty(bankCode) && !BuildConfig.CC_CODE.equals(bankCode)) {
                                boolean isBankAccount = BankAccountHelper.isBankAccount(bankCode);
                                ZPBank zpBank = getCardFromBankConfig(appVersion, bankCode, isBankAccount);
                                if (zpBank == null) {
                                    continue;
                                }
                                zpBank.bankLogo = getBankLogo(bankCode);
                                zpBank.isBankAccount = isBankAccount;
                                if (!supportBank.contains(zpBank)) {
                                    supportBank.add(zpBank);
                                }
                            } else if (!TextUtils.isEmpty(bankCode) && BuildConfig.CC_CODE.equals(bankCode)) {
                                supportBank.add(visa);
                                supportBank.add(masterCard);
                            }
                        }
                    }
                    return Observable.just(supportBank);
                } catch (Exception e) {
                    Log.e(this, e);
                    return Observable.error(e);
                }
            }
        };
    }

    @Override
    public Observable<BankConfigResponse> getBankList(String appVersion, long currentTime) {
        String checksum = mBankListRepository.getLocalStorage().getCheckSum();
        String platform = BuildConfig.PAYMENT_PLATFORM;
        Observable<BankConfigResponse> bankListCache = mBankListRepository
                .getLocalStorage()
                .get()
                .onErrorReturn(null);
        Observable<BankConfigResponse> bankListCloud = mBankListRepository
                .fetchCloud(platform, checksum, appVersion)
                .flatMap(mapResult);
        return Observable.concat(bankListCache, bankListCloud)
                .first(bankConfigResponse -> bankConfigResponse != null && (bankConfigResponse.expiredtime > currentTime))
                .compose(SchedulerHelper.applySchedulers());
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
                .subscribeOn(Schedulers.io())
                .flatMap(supportBanks(appVersion))
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected String getBankLogo(String pBankCode) {
        return String.format("bank_%s%s", pBankCode, Constants.BITMAP_EXTENSION);
    }

    private ZPBank getCardFromBankConfig(String appVersion, String bankCode, boolean isBankAccount) {
        if (TextUtils.isEmpty(bankCode)) {
            return null;
        }
        //get bank status and message in maintenance or need up version for link transtype
        @BankFunctionCode int bankFunctionCode = isBankAccount ? BankFunctionCode.LINK_BANK_ACCOUNT : BankFunctionCode.LINK_CARD;
        BankConfig bankConfig = this.mBankListRepository.getLocalStorage().getBankConfig(bankCode);
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

    @Override
    public String getBankPrefix() {
        return this.mBankListRepository.getLocalStorage().getBankPrefix();
    }

    @Override
    public void clearCheckSum() {
        this.mBankListRepository.getLocalStorage().clearCheckSum();
    }

    @Override
    public void clearConfig() {
        this.mBankListRepository.getLocalStorage().clearConfig();
    }
}

