package vn.com.zalopay.wallet.datasource.request;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.implement.LoadMapBankAccountListImpl;
import vn.com.zalopay.wallet.helper.BankAccountHelper;
import vn.com.zalopay.wallet.listener.IGetBankAccountList;
import vn.com.zalopay.wallet.utils.Log;

/***
 * get map bank account list
 */
public class MapBankAccountListTask extends BaseTask<BankAccountListResponse> {
    private AdapterBase mAdapter;
    private IGetBankAccountList mGetBankAccountCallback;

    public MapBankAccountListTask(AdapterBase pAdapter) {
        super();
        this.mAdapter = pAdapter;
        this.mGetBankAccountCallback = null;
    }

    public MapBankAccountListTask(IGetBankAccountList pGetCardInfoCallBack) {
        super();
        this.mAdapter = null;
        this.mGetBankAccountCallback = pGetCardInfoCallBack;
    }

    @Override
    public void onDoTaskOnResponse(BankAccountListResponse pResponse) {
        Log.d(this, "onDoTaskOnResponse");
        if (pResponse == null || pResponse.returncode != 1) {
            Log.d(this, "request not success...stopping saving response to cache");
            return;
        }
        if (BankAccountHelper.needUpdateMapBankAccountListOnCache(pResponse.bankaccountchecksum)) {
            try {
                BankAccountHelper.saveMapBankAccountListToCache(pResponse.bankaccountchecksum, pResponse.bankaccounts);
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
    }

    @Override
    public void onRequestSuccess(BankAccountListResponse pResponse) {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_GET_BANKACCOUNT_LIST_COMPLETE, pResponse);
        }
        if (mGetBankAccountCallback != null) {
            mGetBankAccountCallback.onGetBankAccountListComplete(pResponse);
        }
    }

    @Override
    public void onRequestFail(Throwable e) {
        BankAccountListResponse bankAccountListResponse = new BankAccountListResponse();
        bankAccountListResponse.returncode = -1;
        bankAccountListResponse.returnmessage = getDefaulErrorNetwork();
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_GET_BANKACCOUNT_LIST_COMPLETE, bankAccountListResponse);
        }
        if (mGetBankAccountCallback != null) {
            mGetBankAccountCallback.onGetBankAccountListComplete(bankAccountListResponse);
        }
    }

    @Override
    public void onRequestInProcess() {
        Log.d(this, "onRequestInProcess");
    }

    @Override
    public String getDefaulErrorNetwork() {
        return GlobalData.getStringResource(RS.string.zpw_alert_network_error_loadmapbankaccountlist);
    }

    @Override
    protected void doRequest() {
        try {
            newDataRepository().setTask(this).loadData(new LoadMapBankAccountListImpl(), getDataParams());
        } catch (Exception ex) {
            onRequestFail(ex);
            Log.e(this, ex);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareGetBankAccountListParams(getDataParams());
            return true;
        } catch (Exception e) {
            onRequestFail(e);
            Log.e(this, e);
        }
        return false;
    }
}
