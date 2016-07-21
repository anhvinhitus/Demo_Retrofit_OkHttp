package vn.com.vng.zalopay.data.api.response.redpacket;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentPackageResponse extends BaseResponse {
    public String revZaloPayID;
    public String revZaloID;
    public String revFullName;
    public String revAvatarURL;
    public long openTime;
    public long amount;
    public String sendMessage;
    public boolean isLuckiest;


}
