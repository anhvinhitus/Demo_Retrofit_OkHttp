package vn.com.zalopay.wallet.business.entity.base;

import android.text.TextUtils;

public class BaseResponse extends BaseEntity<BaseResponse> {
    public int returncode = 0;
    public String returnmessage = null;
    public String accesstoken = null;

    public String suggest_message = null;
    public String suggest_actions = null;

    public String getMessage()
    {
        return !TextUtils.isEmpty(suggest_message) ? suggest_message: returnmessage;
    }
}
