package vn.com.vng.zalopay.mdl;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import timber.log.Timber;
import vn.zing.pay.zmpsdk.ZingMobilePayService;
import vn.zing.pay.zmpsdk.entity.ZPPaymentResult;
import vn.zing.pay.zmpsdk.entity.ZPWPaymentInfo;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentChannel;
import vn.zing.pay.zmpsdk.entity.enumeration.EPaymentStatus;
import vn.zing.pay.zmpsdk.listener.ZPPaymentListener;

/**
 * Created by huuhoa on 5/16/16.
 */
public class ZaloPayIAPNativeModule extends ReactContextBaseJavaModule {
    public ZaloPayIAPNativeModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "ZaloPayIAP";
    }

    /**
     * Tham khảo tài liệu: https://docs.google.com/a/vng.com.vn/document/d/1dYKPBXLF9JRwExXkc5XlQJiQKRxp19Gf8x8cbXvGSvA/edit?usp=sharing
     * @param params Chứa danh sách các thuộc tính cần thiết để gọi hàm thanh toán của SDK
     * @param promise Trả về kết quả thanh toán
     */
    @ReactMethod
    public void payOrder(ReadableMap params, Promise promise) {
        // verify params parameters
        params.getString()


        // call payment SDK
        ZPWPaymentInfo paymentInfo = new ZPWPaymentInfo();
        EPaymentChannel forcedPaymentChannel = EPaymentChannel.LINK_CARD;
        paymentInfo.zaloUserID = String.valueOf(user.uid);
        paymentInfo.zaloPayAccessToken = user.accesstoken;

        ZingMobilePayService.pay(mView.getActivity(), forcedPaymentChannel, paymentInfo, zpPaymentListener);

        // return result
//        zpPaymentListener
    }

    ZPPaymentListener zpPaymentListener = new ZPPaymentListener() {
        @Override
        public void onComplete(ZPPaymentResult zpPaymentResult) {
            if (zpPaymentResult == null) {
//                if (!AndroidUtils.isNetworkAvailable(mView.getContext())) {
//                    mView.showError("Vui lòng kiểm tra kết nối mạng và thử lại.");
//                } else {
//                    mView.showError("Lỗi xảy ra trong quá trình liên kết thẻ. Vui lòng thử lại sau.");
//                }
            } else {
                EPaymentStatus paymentStatus = zpPaymentResult.paymentStatus;
                if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_SUCCESS.getNum()) {
//                    getBalance();
//                    ZPWPaymentInfo paymentInfo = zpPaymentResult.paymentInfo;
//                    if (paymentInfo == null) {
//                        return;
//                    }
//                    mView.onAddCardSuccess(paymentInfo.mappedCreditCard);
                } else if (paymentStatus.getNum() == EPaymentStatus.ZPC_TRANXSTATUS_TOKEN_INVALID.getNum()) {
//                    mView.onTokenInvalid();
                }
            }
        }

        @Override
        public void onCancel() {
//            hideLoadingView();
        }

        @Override
        public void onSMSCallBack(String s) {

        }
    };
}
