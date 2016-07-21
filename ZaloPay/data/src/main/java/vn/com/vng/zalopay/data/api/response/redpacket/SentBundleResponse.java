package vn.com.vng.zalopay.data.api.response.redpacket;

import vn.com.vng.zalopay.data.api.response.BaseResponse;

/**
 * Created by longlv on 15/07/2016.
 */
public class SentBundleResponse extends BaseResponse {
    public long bundleid;
    public String sendzalopayid;
    public int type;
    public long createtime;
    public long lastopentime;
    public int totalluck;
    public int numofopenedpakages;
    public int numofpackages;
}
