package vn.com.zalopay.wallet.business.behavior.gateway;

import android.os.AsyncTask;

import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.listener.ZPWInitResourceListener;
import vn.com.zalopay.wallet.utils.Log;

public class BundleResourceLoader extends AsyncTask<Void, Void, Boolean> {
    private ZPWInitResourceListener mCallBack;

    public BundleResourceLoader(ZPWInitResourceListener pListener) {
        this.mCallBack = pListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            ResourceManager.initResource();
            return ResourceManager.isInit();
        } catch (Exception e) {
            Log.e(this, e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            this.mCallBack.onSuccess();
        } else {
            this.mCallBack.onError(GlobalData.getStringResource(RS.string.zpw_alert_error_resource_not_download));
        }
    }
}
