package vn.com.zalopay.wallet.business.entity.base;

public class BaseResponse extends BaseEntity<BaseResponse> {
    public int returncode = 0;
    public String returnmessage = null;
    public String accesstoken = null;
    public String suggestmessage = null;
    public int[] suggestactions = null;

    public String getMessage() {
        return returnmessage;
    }

    public String getSuggestMessage() {
        return suggestmessage;
    }
}
