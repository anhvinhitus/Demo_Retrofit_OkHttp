package vn.com.vng.zalopay.mdl.error;

/**
 * Created by longlv on 16/05/2016.
 */
public class PaymentError {

    private static final String TAG = PaymentError.class.getSimpleName();

    //thông tin đầu vào thiếu hoặc không hợp lệ.
    public static int ERR_CODE_INPUT = 6000;

    //thông tin user thiếu hoặc không hợp lệ.
    public static int ERR_CODE_USER_INFO = 6001;

    //data thiếu hoặc không hợp lệ.
    public static int ERR_CODE_DATA = 7000;

    //lỗi mang
    public static int ERR_CODE_INTERNET = 4000;

    //lỗi hệ thống
    public static int ERR_CODE_SYSTEM = 5000;

    //none error
    public static int ERR_CODE_NONE = 0;

    //internal timeout
    public static int ERR_CODE_INTERNAL_TIMEOUT = -9999;

    public static String getErrorMessage(int errorCode) {
        String errorMessage = "";
        if (errorCode == ERR_CODE_INPUT) {
            errorMessage = "Thông tin đầu vào thiếu hoặc không hợp lệ.";
        } else if (errorCode == ERR_CODE_USER_INFO) {
            errorMessage = "Thông tin người dùng không hợp lệ.";
        } else if (errorCode == ERR_CODE_INTERNET) {
            errorMessage = "Lỗi kết nối mạng.";
        } else if (errorCode == ERR_CODE_SYSTEM) {
            errorMessage = "Lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại sau.";
        } else if (errorCode == ERR_CODE_DATA) {
            errorMessage = "Dữ liệu đầu vào thiếu hoặc không hợp lệ.";
        } else if (errorCode == ERR_CODE_NONE) {
            errorMessage = "";
        } else if (errorCode == ERR_CODE_INTERNAL_TIMEOUT) {
            errorMessage = "Internal timeout";
        }
        return errorMessage;
    }
}
