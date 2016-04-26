package vn.zing.pay.zmpsdk.business.card;

import vn.zing.pay.zmpsdk.business.TAbtractPaymentTask;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.entity.DResponseGetStatus;
import vn.zing.pay.zmpsdk.entity.enumeration.EEventType;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest;
import vn.zing.pay.zmpsdk.helper.HttpClientRequest.Type;
import vn.zing.pay.zmpsdk.utils.GsonUtils;
import vn.zing.pay.zmpsdk.utils.Log;
import vn.zing.pay.zmpsdk.view.dialog.DialogManager;
import android.text.TextUtils;

public class TSubmitCardTask extends TAbtractPaymentTask {
	private String mUrl = Constants.getUrlPrefix() + Constants.URL_CARD_SUBMIT_CARD;

	public TSubmitCardTask(AdapterCard adapter) {
		super(adapter);
	}

	@Override
	protected String doInBackground(Void... paramVarArgs) {
		Log.i("Zmp", "TSubmitCardTask.doInBackground...");
		HttpClientRequest request = new HttpClientRequest(Type.POST, mUrl);
		try {
			putPaymentInfo(request);
			request.addParams("cardCode", ((AdapterCard) mAdapter).getCardCode());
			request.addParams("cardSerialNo", ((AdapterCard) mAdapter).getCardSerialNo());
			return request.getText();
		} catch (Exception ex) {
			Log.e("Zmp", ex);
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		DResponseGetStatus response = null;
		if (!TextUtils.isEmpty(result)) {
			response = GsonUtils.fromJsonString(result, DResponseGetStatus.class);

			// TODO: Remove this
			if (Log.IS_LOG_ENABLE) {
				DialogManager.showAlertDialog(result);
			}
		}
		mAdapter.onEvent(EEventType.ON_SUBMIT_COMPLETED, response);
	}
}
