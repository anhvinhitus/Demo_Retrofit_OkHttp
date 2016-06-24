package vn.com.vng.zalopay.data.ws.message;

/**
 * Created by AnhHieu on 6/20/16.
 */
public class TransactionType {
    // transtype : 1 , thanh toan, 2 :  nap tien vao vi, 3 lien ket the, 4 : chuyen tien vao tai khoan zalopay
    public static final int THANH_TOAN = 1;
    public static final int NAP_TIEN_VAO_VI = 2;
    public static final int LIEN_KET_THE = 3;
    public static final int CHUYEN_TIEN_VAO_ZALO_PAY = 4;

    public static String getTitle(int type) {
        if (type == THANH_TOAN) {
            return "Thanh Toán";
        } else if (type == NAP_TIEN_VAO_VI) {
            return " Gửi tiền vào tài khoản Zalo Pay";
        } else if (type == LIEN_KET_THE) {
            return "Đã lưu thẻ";
        } else if (type == CHUYEN_TIEN_VAO_ZALO_PAY) {
            return "Gửi tiền vào tài khoản Zalo Pay";
        }
        return "Zalo Pay";
    }
}
