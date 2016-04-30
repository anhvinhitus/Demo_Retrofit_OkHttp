package vn.zing.pay.trivialdrivesample;

import java.util.ArrayList;
import java.util.List;

import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.data.Constants;
import vn.zing.pay.zmpsdk.data.Constants.HostType;
import vn.zing.pay.zmpsdk.data.sqllite.GoogleIABReceiptDataSource;
import vn.zing.pay.zmpsdk.entity.ZPPaymentInfo;
import vn.zing.pay.zmpsdk.entity.ZPPaymentItem;
import vn.zing.pay.zmpsdk.entity.ZPPaymentOption;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.google.DGoogleIabReceipt;
import vn.zing.pay.zmpsdk.helper.gms.GcmMessageHandler;
import vn.zing.pay.zmpsdk.helper.gms.MyInstanceIDListenerService;
import vn.zing.pay.zmpsdk.helper.gms.RegistrationIntentService;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;
import vn.zing.pay.zmpsdk.utils.DeviceUtil;
import vn.zing.pay.zmpsdk.view.dialog.PaymentAlertDialog;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

@SuppressWarnings("deprecation")
public class DemoSDKActivity extends ActionBarActivity {

	private RadioGroup radioHostTypeGroup;
	private EditText username;
	private EditText itemID;
	private EditText itemName;
	private EditText itemPrice;
	private EditText itemQuantity;
	private EditText desc;
	private EditText dispInfo;
	private EditText dispName;
	private EditText sku;

	private String skuID = "g199";
	private String pmcOption = "Exclude:";
	private String key = "E3kCLDkLL2GDhaYhEahsbviSfzwSCDXi";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);

		radioHostTypeGroup = (RadioGroup) findViewById(R.id.radioGroupHostType);

		Spinner skuIdList = (Spinner) findViewById(R.id.skuIdList);
		ArrayAdapter<CharSequence> skuAdapter = ArrayAdapter.createFromResource(this, R.array.sku_array,
				android.R.layout.simple_spinner_item);
		skuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		skuIdList.setAdapter(skuAdapter);
		skuIdList.setOnItemSelectedListener(mItemSelectedListener);

		Spinner spinner = (Spinner) findViewById(R.id.channelOptionList);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.channel_option,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(mItemPmcOptionSelectedListener);

		username = (EditText) findViewById(R.id.editTextUsername);
		itemID = (EditText) findViewById(R.id.editTextIID);
		itemName = (EditText) findViewById(R.id.editTextIName);
		itemPrice = (EditText) findViewById(R.id.editTextPrice);
		itemQuantity = (EditText) findViewById(R.id.editTextQty);
		desc = (EditText) findViewById(R.id.editTextDesc);
		dispInfo = (EditText) findViewById(R.id.editTextDispInfo);
		dispName = (EditText) findViewById(R.id.editTextDispName);
		sku = (EditText) findViewById(R.id.editTextSkuId);

		((View) findViewById(R.id.btn)).setOnClickListener(onClickListener);

		((View) findViewById(R.id.btnHash)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((EditText) findViewById(R.id.editTxt)).setText(DeviceUtil
						.getCertificateSHA1Fingerprint(DemoSDKActivity.this.getApplicationContext()));

				GoogleIABReceiptDataSource dataSource = new GoogleIABReceiptDataSource(DemoSDKActivity.this);
				List<DGoogleIabReceipt> all = dataSource.getAll();
				Log.i("DATABASE", all.toString());

				Intent intent = new Intent(DemoSDKActivity.this, WebBridgeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivityForResult(intent, 54321);
			}
		});

		onStartService();
	}

	// Starts the IntentService
	public void onStartService() {
	}

	OnItemSelectedListener mItemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			// An item was selected. You can retrieve the selected item using
			skuID = (String) parent.getItemAtPosition(pos);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Another interface callback
		}
	};

	OnItemSelectedListener mItemPmcOptionSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			// An item was selected. You can retrieve the selected item using
			pmcOption = (String) parent.getItemAtPosition(pos);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Another interface callback
		}
	};

	View.OnClickListener onClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// ////////////////////////////////////
			// ////////// PMT DEV ONLY ////////////
			// ////////////////////////////////////
			int selectedHost = radioHostTypeGroup.getCheckedRadioButtonId();
            if (selectedHost == R.id.checkLive) {
                Constants.setUrlPrefix(HostType.LIVE);
            } else if (selectedHost == R.id.checkStaging) {
                Constants.setUrlPrefix(HostType.STAGING);
            } else if (selectedHost == R.id.checkSandbox) {
                Constants.setUrlPrefix(HostType.SANDBOX);
            } else {
                Constants.setUrlPrefix(HostType.LIVE);
            }
			// ////////////////////////////////////
			// ////////////////////////////////////

			ZPPaymentInfo paymentInfo = new ZPPaymentInfo();
			paymentInfo.appTime = System.currentTimeMillis();
			paymentInfo.appTransID = "Android_" + System.currentTimeMillis();
			// unique id cho giao dịch
			paymentInfo.appID = 1;
			// App id đã đăng ký với Zalo Platform
			paymentInfo.items = new ArrayList<ZPPaymentItem>();

			ZPPaymentItem item = new ZPPaymentItem();
			item.itemID = DemoSDKActivity.this.itemID.getText().toString();
			item.itemName = DemoSDKActivity.this.itemName.getText().toString();
			item.itemPrice = (DemoSDKActivity.this.itemPrice.getText() == null) ? 0 : Long
					.parseLong(DemoSDKActivity.this.itemPrice.getText().toString());
			item.itemQuantity = (DemoSDKActivity.this.itemQuantity.getText() == null) ? 0 : Long
					.parseLong(DemoSDKActivity.this.itemQuantity.getText().toString());
			paymentInfo.items.add(item);
			paymentInfo.amount = item.itemPrice * item.itemQuantity;
			// Tổng itemPrice của từng ZaloPaymentItem. Amount truyền =0 nếu
			// danh sách items không có. Nếu amount > 0 thì items không được
			// rỗng

			paymentInfo.description = DemoSDKActivity.this.desc.getText().toString();
			paymentInfo.displayInfo = DemoSDKActivity.this.dispInfo.getText().toString();
			paymentInfo.displayName = DemoSDKActivity.this.dispName.getText().toString();
			paymentInfo.embedData = "Nhung";
			// Thông tin nhúng trong hóa đơn
			paymentInfo.skuID = (TextUtils.isEmpty(sku.getText().toString())) ? skuID : sku.getText().toString();
			// id cho sản phẩm thanh toán qua Google wallet
			paymentInfo.appUser = DemoSDKActivity.this.username.getText().toString(); // thông tin
			paymentInfo.mac = ZingMobilePayService.generateHMAC(paymentInfo, 1, key);

			EPaymentChannel forcedPaymentChannel = null;
			List<EPaymentChannel> excludedPaymentChannels = new ArrayList<EPaymentChannel>();

			if (((CheckBox) findViewById(R.id.checkATM)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.ATM);
			}
			if (((CheckBox) findViewById(R.id.checkCredit)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.CREDIT_CARD);
			}
			if (((CheckBox) findViewById(R.id.checkGoogle)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.GOOGLE_WALLET);
			}
			if (((CheckBox) findViewById(R.id.checkMobi)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.TELCO_MOBI);
			}
			if (((CheckBox) findViewById(R.id.checkSMS)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.SMS);
			}
			if (((CheckBox) findViewById(R.id.checkViettel)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.TELCO_VIETTEL);
			}
			if (((CheckBox) findViewById(R.id.checkVina)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.TELCO_VINAPHONE);
			}
			if (((CheckBox) findViewById(R.id.checkZingCard)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.ZING_CARD);
			}
			if (((CheckBox) findViewById(R.id.checkMergeCard)).isChecked()) {
				excludedPaymentChannels.add(forcedPaymentChannel = EPaymentChannel.MERGE_CARD);
			}
			ZPPaymentOption paymentOption = new ZPPaymentOption((pmcOption.equals("Exclude:")) ? null
					: forcedPaymentChannel/*, excludedPaymentChannels*/);

			ZingMobilePayService.pay(DemoSDKActivity.this, forcedPaymentChannel, paymentInfo, new ZPPaymentListener() {

				@Override
				public void onSMSCallBack(final String appTransID) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							PaymentAlertDialog alertDlg = new PaymentAlertDialog(DemoSDKActivity.this);
							alertDlg.showAlert("SMS CallBack: " + appTransID);
						}
					});
				}

				@Override
				public void onComplete(final ZPPaymentResult pPaymentResult) {

					Log.i("ZMP", "onComplete CallBack: " + pPaymentResult.toJsonString());
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							pPaymentResult.purchase = null;
							PaymentAlertDialog alertDlg = new PaymentAlertDialog(DemoSDKActivity.this);
							alertDlg.showAlert("onComplete CallBack: " + pPaymentResult.toJsonString());
						}
					});
				}

				@Override
				public void onCancel() {
					Log.i("ZMP", "==== onCancel ====");
				}
			});
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("ZMP", requestCode + "|" + requestCode + "|" + ((data == null) ? "null" : data.toString()));
	};
}
