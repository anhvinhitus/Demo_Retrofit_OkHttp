package vn.com.vng.zalopay.data.api.response.redpacket;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class PackageInBundleResponse extends BaseResponse {
    public long packageid;
    public long bundleid;
    public String revzalopayid;
    public long revzaloid;
    public String revfullname;
    public String revavatarurl;
    public long opentime;
    public long amount;
    public String sendmessage;
    public boolean isluckiest;
}
