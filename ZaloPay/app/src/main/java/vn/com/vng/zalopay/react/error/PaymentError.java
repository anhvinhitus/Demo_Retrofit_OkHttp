package vn.com.vng.zalopay.react.error;

import timber.log.Timber;

/**
 * Created by longlv on 16/05/2016.
 * Define error code & error message for PaymentWrapper
 */
public enum  PaymentError {
    ERR_CODE_FAIL(-1),
    ERR_CODE_UNKNOWN(0),
    ERR_CODE_SUCCESS(1),
    ERR_CODE_INPUT(2),
    ERR_CODE_INTERNET(3),
    ERR_CODE_TOKEN_INVALID(-3),
    ERR_CODE_USER_CANCEL(4),
    ERR_CODE_PROCESSING(5),
    ERR_CODE_SERVICE_MAINTENANCE(6),
    ERR_CODE_MONEY_NOT_ENOUGH(7),
    ERR_CODE_SYSTEM(5000),
    ERR_CODE_USER_INFO(5001);

    private int value;

    PaymentError(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static String getErrorMessage(PaymentError paymentError) {
        Timber.d("getErrorMessage error [%s]", paymentError);
        String errorMessage;
        if (paymentError == ERR_CODE_SUCCESS) {
            errorMessage = "Giao dịch thành công.";
        } else if (paymentError == ERR_CODE_FAIL) {
            errorMessage = "Giao dịch thất bại.";
        } else if (paymentError == ERR_CODE_PROCESSING) {
            errorMessage = "Giao dịch đang được xử lý.";
        } else if (paymentError == ERR_CODE_SERVICE_MAINTENANCE) {
            errorMessage = "Đang bảo trì hệ thống.";
        } else if (paymentError == PaymentError.ERR_CODE_MONEY_NOT_ENOUGH) {
            errorMessage = "Tài khoản không đủ để thực hiện giao dịch.";
        } else if (paymentError == ERR_CODE_INPUT) {
            errorMessage = "Thông tin đầu vào thiếu hoặc không hợp lệ.";
        } else if (paymentError == ERR_CODE_INTERNET) {
            errorMessage = "Vui lòng kiểm tra kết nối mạng và thử lại.";
        } else if (paymentError == ERR_CODE_SYSTEM) {
            errorMessage = "Lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại sau.";
        } else if (paymentError == ERR_CODE_USER_INFO) {
            errorMessage = "Thông tin người dùng không hợp lệ.";
        } else if (paymentError == ERR_CODE_USER_CANCEL) {
            errorMessage = "Người dùng huỷ bỏ giao dịch.";
        } else if (paymentError == ERR_CODE_TOKEN_INVALID) {
            errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!";
        } else {
            errorMessage = String.format("Lỗi xảy ra trong quá trình thanh toán.[%s]", paymentError);
        }
        return errorMessage;
    }
}
