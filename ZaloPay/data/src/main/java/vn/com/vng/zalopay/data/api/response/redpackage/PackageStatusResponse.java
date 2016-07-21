package vn.com.vng.zalopay.data.api.response.redpackage;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 18/07/2016.
 * Data of getpackagestatus request
 */
public class PackageStatusResponse extends BaseResponse {
    public boolean isProcessing;
    public long amount;
    public String zpTransID;
    public String nextAction;
    public String data;
    public long balance;
}
