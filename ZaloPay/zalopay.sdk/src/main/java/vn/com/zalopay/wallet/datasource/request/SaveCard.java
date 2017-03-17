package vn.com.zalopay.wallet.datasource.request;

import android.os.AsyncTask;
import android.text.TextUtils;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.SaveCardResponse;
import vn.com.zalopay.wallet.business.entity.enumeration.EEventType;
import vn.com.zalopay.wallet.controller.SDKApplication;
import vn.com.zalopay.wallet.datasource.DataParameter;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.SaveCardImpl;
import vn.com.zalopay.wallet.helper.MapCardHelper;
import vn.com.zalopay.wallet.listener.ZPWSaveMapCardListener;
import vn.com.zalopay.wallet.utils.Log;

public class SaveCard extends BaseRequest<SaveCardResponse> {
    private String mTransID;
    private AdapterBase mAdapter;
    private ZPWSaveMapCardListener mSaveCardListener;
    private String mErrorMessage;
    private int countRetry;

    public SaveCard(AdapterBase pAdapter, String pTransID) {
        super();

        mAdapter = pAdapter;
        mTransID = pTransID;
        mErrorMessage = null;
        countRetry = 0;
    }

    public void setOnSaveCardListener(ZPWSaveMapCardListener pListener) {
        this.mSaveCardListener = pListener;
    }

    private void retry() {
        countRetry++;
        makeRequest();
    }

    @Override
    protected void onRequestSuccess() throws Exception {
        SaveCardResponse saveCardResponse = null;

        if (getResponse() instanceof SaveCardResponse) {
            saveCardResponse = (SaveCardResponse) getResponse();
        }

        if (saveCardResponse == null && countRetry < Constants.MAX_COUNT_RETRY_SAVE_CARD) {
            retry();
            return;
        }

        if (mAdapter != null) {
            mAdapter.onEvent(EEventType.ON_SAVE_CARD, saveCardResponse);
        }

        if (mSaveCardListener != null) {
            if (saveCardResponse != null) {
                if (saveCardResponse.returncode > 0) {
                    TSaveCardThreadTask saveCardThreadTask = new TSaveCardThreadTask();
                    saveCardThreadTask.execute();
                } else
                    mSaveCardListener.onError(saveCardResponse.getMessage());
            } else {
                mSaveCardListener.onError(null);
            }
        }
    }

    @Override
    protected void onRequestFail(String pMessage) {

    }

    @Override
    protected void onRequestInProcess() {

    }

    @Override
    protected void doRequest() {
        try {
            shareDataRepository().pushData(new SaveCardImpl(), getDataParams());
        } catch (Exception ex) {
            Log.e(this, ex);
            onRequestFail(null);
        }
    }

    @Override
    protected boolean doParams() {
        try {
            DataParameter.prepareSaveCardParams(getDataParams(), mTransID);
        } catch (Exception e) {
            Log.e(this, e);
            onRequestFail(GlobalData.getStringResource(RS.string.zpw_string_error_layout));
            return false;
        }

        return true;
    }

    /***
     * THREAD SAVE CARD TO CACEHD.
     */
    public class TSaveCardThreadTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... pParams) {

            try {
                mErrorMessage = MapCardHelper.saveMapCardToCache(mTransID);

                if (TextUtils.isEmpty(mErrorMessage)) {
                    return true;
                }
            } catch (Exception e) {
                Log.e(this, e);
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result && TextUtils.isEmpty(mErrorMessage))
                mSaveCardListener.onSuccess();
            else {
                String errMess = mErrorMessage;
                if (TextUtils.isEmpty(errMess))
                    errMess = GlobalData.getStringResource(RS.string.zpw_string_alert_card_save_error);

                mSaveCardListener.onError(errMess);
            }
        }

    }

}
