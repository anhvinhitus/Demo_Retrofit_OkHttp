package vn.com.zalopay.wallet.datasource.request;

import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.GetBankAccountListImpl;
import vn.com.zalopay.wallet.listener.IGetBankAccountList;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.Log;

/***
 * get bank account list
 */
public class GetBankAccountList extends BaseRequest<BankAccountListResponse> {
    private AdapterBase mAdapter;
    private IGetBankAccountList mGetBankAccountCallback;

    public GetBankAccountList(AdapterBase pAdapter) {
        super();
        this.mAdapter = pAdapter;
        this.mGetBankAccountCallback = null;
    }

    public GetBankAccountList(IGetBankAccountList pGetCardInfoCallBack) {
        super();
        this.mAdapter = null;
        this.mGetBankAccountCallback = pGetCardInfoCallBack;
    }

    private void onPostResult() {
        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_GET_BANKACCOUNT_LIST_COMPLETE, getResponse());
        }
        if (mGetBankAccountCallback != null) {
            mGetBankAccountCallback.onGetBankAccountListComplete(getResponse());
        }
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        onPostResult();
    }

    @Override
    protected void onRequestFail(String pMessage) {
        if (!TextUtils.isEmpty(pMessage)) {
            if (getResponse() == null) {
                createReponse(-1, pMessage);
            }
            getResponse().returncode = -1;
            getResponse().returnmessage = pMessage;
        }
        onPostResult();
    }

    @Override
    protected void createReponse(int pCode, String pMessage) {
        mResponse = new BankAccountListResponse();
    }

    @Override
    protected void onRequestInProcess() {

    }

    @Override
    protected void doRequest() {
        try {
            // Lost connection
            if (!ConnectionUtil.isOnline(GlobalData.getAppContext()) && mAdapter != null) {
                mAdapter.showProgressBar(false, null);
                mAdapter.getActivity().showWarningDialog(null, GlobalData.getStringResource(RS.string.zpw_string_get_bank_account_error));
                return;
            }

           newDataRepository().getData(new GetBankAccountListImpl(), getDataParams());
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareGetBankAccountListParams(getDataParams());
            return true;
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
        }
        return false;
    }
}
