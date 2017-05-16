package vn.com.zalopay.analytics;

/**
 * Created by khattn on 4/28/17.
 * Constants track apptransid
 */

public interface ZPPaymentSteps {
    static int OrderStep_GetAppInfo = 1; // gọi getappinfo
    static int OrderStep_SDKInit = 2; // gọi show sdk (sdk get platfrom info + check bill)
    static int OrderStep_InputCardInfo = 3; // user nhập thông tin thẻ thanh toán
    static int OrderStep_ChoosePayMethod = 4; // user chọn kênh thanh toán
    static int OrderStep_SubmitTrans = 5; // gửi transacion lên server
    static int OrderStep_VerifyOtp = 6; // authen payer qua đường api (otp)
    static int OrderStep_WebLogin = 7; // user đăng nhập tài khoản internet banking
    static int OrderStep_WebInfoConfirm = 8; // user xác nhận thông tin, VCB: nhập mật khẩu + captcha, VTB: nhập catpcha, BIDV: nhập mật khẩu + captcha
    static int OrderStep_WebOtp = 9; // user nhập otp để hoàn thành giao dịch
    static int OrderStep_OrderResult = 10; // kêt quả thanh toán sau khi gettranstatus

    static int OrderSource_QR = 1; // Thanh toán khi quét QR
    static int OrderSource_AppToApp = 2; // Thanh toán app to app
    static int OrderSource_WebToApp = 3; // Thanh toán khi web gọi qua app
    static int OrderSource_MerchantApp = 4; // Thanh toán merchant app inside Zalo Pay
    static int OrderSource_NotifyInApp = 5; // Thanh toán khi nhận dc notification chứa bill
    static int OrderSource_Bluetooth = 6; // Thanh toán khi có hóa đơn từ Bluetooth
    static int OrderSource_NFC = 7; // Thanh toán khi có hóa đơn từ NFC

    static int OrderStepResult_None = 0;
    static int OrderStepResult_Success = 1; // Step thành công
    static int OrderStepResult_Fail = 2; // Step thất bại
    static int OrderStepResult_UserCancel = 3; // user cancel (user chọn back khi thanh toán)
}
