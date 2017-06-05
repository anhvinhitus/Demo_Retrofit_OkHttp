package vn.com.zalopay.analytics;

/**
 * Created by khattn on 4/28/17.
 * Constants track apptransid
 */

public interface ZPPaymentSteps {
     int OrderStep_GetAppInfo = 1; // gọi getappinfo
     int OrderStep_SDKInit = 2; // gọi show sdk (sdk get platfrom info + check bill)
     int OrderStep_InputCardInfo = 3; // user nhập thông tin thẻ thanh toán
     int OrderStep_ChoosePayMethod = 4; // user chọn kênh thanh toán
     int OrderStep_SubmitTrans = 5; // gửi transacion lên server
     int OrderStep_VerifyOtp = 6; // authen payer qua đường api (otp)
     int OrderStep_WebLogin = 7; // user đăng nhập tài khoản internet banking
     int OrderStep_WebInfoConfirm = 8; // user xác nhận thông tin, VCB: nhập mật khẩu + captcha, VTB: nhập catpcha, BIDV: nhập mật khẩu + captcha
     int OrderStep_WebOtp = 9; // user nhập otp để hoàn thành giao dịch
     int OrderStep_OrderResult = 10; // kêt quả thanh toán sau khi gettranstatus

     int OrderSource_QR = 1; // Thanh toán khi quét QR
     int OrderSource_AppToApp = 2; // Thanh toán app to app
     int OrderSource_WebToApp = 3; // Thanh toán khi web gọi qua app
     int OrderSource_MerchantApp = 4; // Thanh toán merchant app inside Zalo Pay
     int OrderSource_NotifyInApp = 5; // Thanh toán khi nhận dc notification chứa bill
     int OrderSource_Bluetooth = 6; // Thanh toán khi có hóa đơn từ Bluetooth
     int OrderSource_NFC = 7; // Thanh toán khi có hóa đơn từ NFC
     int OrderSource_Unknown = 0; // Truờng hợp gọi thanh toán SDK (withdraw, link account, link bank) không có order source

     int OrderStepResult_None = 0;
     int OrderStepResult_Success = 1; // Step thành công
     int OrderStepResult_Fail = 2; // Step thất bại
     int OrderStepResult_UserCancel = 3; // user cancel (user chọn back khi thanh toán)
}
