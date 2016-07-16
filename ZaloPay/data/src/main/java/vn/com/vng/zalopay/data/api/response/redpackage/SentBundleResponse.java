package vn.com.vng.zalopay.data.api.response.redpackage;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundleResponse extends BaseResponse {
    public long bundleID;
    public int type;
    public long createTime;
    public long lastOpenTime;
    public int totalLuck;
    public int numOfOpenedPakages;
    public int numOfPackages;
}
