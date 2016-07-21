package vn.com.vng.zalopay.data.api.response.redpacket;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class ReceivePackageResponse extends BaseResponse {
    public long packageid;
    public long bundleid;
    public String revzalopayid;
    public String sendzalopayid;
    public String sendfullname;
    public long amount;
    public long openedtime;
}
