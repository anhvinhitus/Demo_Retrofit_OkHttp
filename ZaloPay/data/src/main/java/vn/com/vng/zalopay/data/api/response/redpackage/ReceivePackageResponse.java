package vn.com.vng.zalopay.data.api.response.redpackage;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class ReceivePackageResponse extends BaseResponse {
    public long packageID;
    public long bundleID;
    public String sendZaloPayID;
    public String sendFullName;
    public long amount;
    public long openedTime;
}
