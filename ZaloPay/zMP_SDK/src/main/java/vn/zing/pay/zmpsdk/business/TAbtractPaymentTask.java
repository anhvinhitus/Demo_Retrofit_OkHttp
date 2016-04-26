package vn.zing.pay.zmpsdk.business;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.GlobalData;
import vn.zing.pay.zmpsdk.data.Resource;
import vn.zing.pay.zmpsdk.entity.DResponse;
import vn.zing.pay.zmpsdk.entity.ZPPaymentInfo;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.utils.ConnectionUtil;
import vn.zing.pay.zmpsdk.utils.DeviceUtil;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

public abstract class TAbtractPaymentTask extends AsyncTask<Void, Void, String> {

	private static int ERROR_CODE_INVALID_HMAC = 0;
	private static int ERROR_CODE_EXPIRED_HMAC = 0;

	static {
		try {
			ERROR_CODE_INVALID_HMAC = Integer.parseInt(GlobalData
					.getStringResource(Resource.string.zingpaysdk_conf_error_code_invalid_hmac));
			ERROR_CODE_EXPIRED_HMAC = Integer.parseInt(GlobalData
					.getStringResource(Resource.string.zingpaysdk_conf_error_code_expired_hmac));
		} catch (Exception ex) {
			Log.e("TAbtractPaymentTask", ex);
		}
	}

	protected AdapterBase mAdapter;

	public TAbtractPaymentTask(AdapterBase adapter) {
		this.mAdapter = adapter;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		try {
			if (mAdapter != null) {
				DialogManager.showProcessDialog(null, null);
			}
		} catch (Exception ex) {
			Log.e(this, ex);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		DialogManager.closeProcessDialog();

		try {
			if (TextUtils.isEmpty(result)) {
				DialogManager.showAlertDialog(null);
			} else {
				DResponse response = GsonUtils.fromJsonString(result, DResponse.class);
				if (response.returnCode == ERROR_CODE_EXPIRED_HMAC || response.returnCode == ERROR_CODE_INVALID_HMAC) {
					mAdapter.finish(GlobalData.getStringResource(Resource.string.zingpaysdk_alert_atm_expired));
				}
			}
		} catch (Exception ex) {
			DialogManager.showAlertDialog(null);
			Log.e(this, ex);
		}
	}

	protected void putPaymentInfo(HttpClientRequest request) throws UnsupportedEncodingException {
		putBasicInfo(request);

		if (mAdapter != null) {
			request.addParams("pmcID", String.valueOf(mAdapter.getChannelID()));
		}
	}

	public static void putBasicInfo(HttpClientRequest request) throws UnsupportedEncodingException {
		ZPPaymentInfo paymentInfo = GlobalData.getPaymentInfo();

		request.addParams("appID", String.valueOf(paymentInfo.appID));
		request.addParams("appTransID", paymentInfo.appTransID);
		request.addParams("appUser", paymentInfo.appUser);
		request.addParams("appTime", String.valueOf(paymentInfo.appTime));

		request.addParams("skuID", paymentInfo.skuID);
		if (paymentInfo.items != null) {
			String itemsJsonStr = GsonUtils.toJsonString(paymentInfo.items);
			request.addParams("items", itemsJsonStr);
		}
		request.addParams("description", URLEncoder.encode(paymentInfo.description, "utf-8"));
		request.addParams("embedData", paymentInfo.embedData);
		request.addParams("amount", String.valueOf(paymentInfo.amount));

		request.addParams("mac", paymentInfo.mac);

		request.addParams("platform", "android");
		request.addParams("platformCode", "android");

		request.addParams("deviceID", DeviceUtil.getUniqueDeviceID(GlobalData.getOwnerActivity()));
		request.addParams("appVer", String.valueOf(DeviceUtil.getAppVersion()));

		request.addParams("sdkVer", Constants.VERSION);
		request.addParams("osVer", Build.VERSION.RELEASE);
		request.addParams("connType", ConnectionUtil.getConnectionType(GlobalData.getOwnerActivity()));

		// NOTE: Don't remove this if-block
		// In case of dual-sim device, the "mNetworkOperator" parameter will be
		// duplicated when user purchase via SMS payment channel. And then,
		// server will always use this first-value instead of the value set by
		// SMS adapter.
		if (ConnectionUtil.isDualSim(GlobalData.getOwnerActivity())) {
			request.addParams("mNetworkOperator", ConnectionUtil.getSimOperator(GlobalData.getOwnerActivity()));
		}
	}
}
