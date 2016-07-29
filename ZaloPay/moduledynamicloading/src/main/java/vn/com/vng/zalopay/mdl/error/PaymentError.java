package vn.com.vng.zalopay.mdl.error;

/**
 * Created by longlv on 16/05/2016.
 */
public class PaymentError {

    private static final String TAG = PaymentError.class.getSimpleName();

    //thành công
    public static int ERR_CODE_SUCCESS = 1;

    //thông tin đầu vào thiếu hoặc không hợp lệ.
    public static int ERR_CODE_INPUT = 2;

    //lỗi mang
    public static int ERR_CODE_INTERNET = 3;

    //none error
    public static int ERR_CODE_TOKEN_INVALID = -3;

    //none error
    public static int ERR_CODE_USER_CANCEL = 4;

    //lỗi hệ thống
    public static int ERR_CODE_SYSTEM = 5000;

    public static int ERR_CODE_UNKNOWN = 0;

    //thông tin user thiếu hoặc không hợp lệ.
    public static int ERR_CODE_USER_INFO = 5001;

    public static String getErrorMessage(int errorCode) {
        String errorMessage = "";
        if (errorCode == ERR_CODE_SUCCESS) {
            errorMessage = "Giao dịch thành công.";
        } else if (errorCode == ERR_CODE_INPUT) {
            errorMessage = "Thông tin đầu vào thiếu hoặc không hợp lệ.";
        } else if (errorCode == ERR_CODE_INTERNET) {
            errorMessage = "Lỗi kết nối mạng.";
        } else if (errorCode == ERR_CODE_SYSTEM) {
            errorMessage = "Lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại sau.";
        } else if (errorCode == ERR_CODE_USER_INFO) {
            errorMessage = "Thông tin người dùng không hợp lệ.";
        } else if (errorCode == ERR_CODE_USER_CANCEL) {
            errorMessage = "Người dùng huỷ bỏ giao dịch.";
        } else if (errorCode == ERR_CODE_TOKEN_INVALID) {
            errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!";
        }
        return errorMessage;
    }
}
