package vn.com.vng.zalopay.react.error;

import timber.log.Timber;

/**
 * Created by longlv on 16/05/2016.
 * Define error code & error message for PaymentWrapper
 */
public class PaymentError {

    public static int ERR_CODE_FAIL = -1;
    public static int ERR_CODE_UNKNOWN = 0;
    //thành công
    public static int ERR_CODE_SUCCESS = 1;

    //thông tin đầu vào thiếu hoặc không hợp lệ.
    public static int ERR_CODE_INPUT = 2;

    //lỗi mang
    public static int ERR_CODE_INTERNET = 3;

    //lỗi đầu vào
    public static int ERR_CODE_TOKEN_INVALID = -3;

    //user hủy bỏ giao dịch
    public static int ERR_CODE_USER_CANCEL = 4;

    //Đang xử lý giao dịch
    public static int ERR_CODE_PROCESSING = 5;

    //Hệ thống bảo trì
    public static int ERR_CODE_SERVICE_MAINTENANCE = 6;

    //lỗi hệ thống
    public static int ERR_CODE_SYSTEM = 5000;

    //thông tin user thiếu hoặc không hợp lệ.
    public static int ERR_CODE_USER_INFO = 5001;

    public static String getErrorMessage(int errorCode) {
        Timber.d("getErrorMessage error [%s]", errorCode);
        String errorMessage;
        if (errorCode == ERR_CODE_SUCCESS) {
            errorMessage = "Giao dịch thành công.";
        } else if (errorCode == ERR_CODE_FAIL) {
            errorMessage = "Giao dịch thất bại.";
        } else if (errorCode == ERR_CODE_PROCESSING) {
            errorMessage = "Giao dịch đang được xử lý.";
        } else if (errorCode == ERR_CODE_SERVICE_MAINTENANCE) {
            errorMessage = "Đang bảo trì hệ thống.";
        } else if (errorCode == ERR_CODE_INPUT) {
            errorMessage = "Thông tin đầu vào thiếu hoặc không hợp lệ.";
        } else if (errorCode == ERR_CODE_INTERNET) {
            errorMessage = "Vui lòng kiểm tra kết nối mạng và thử lại.";
        } else if (errorCode == ERR_CODE_SYSTEM) {
            errorMessage = "Lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại sau.";
        } else if (errorCode == ERR_CODE_USER_INFO) {
            errorMessage = "Thông tin người dùng không hợp lệ.";
        } else if (errorCode == ERR_CODE_USER_CANCEL) {
            errorMessage = "Người dùng huỷ bỏ giao dịch.";
        } else if (errorCode == ERR_CODE_TOKEN_INVALID) {
            errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!";
        } else {
            errorMessage = String.format("Lỗi xảy ra trong quá trình thanh toán.[%s]", errorCode);
        }
        return errorMessage;
    }
}
