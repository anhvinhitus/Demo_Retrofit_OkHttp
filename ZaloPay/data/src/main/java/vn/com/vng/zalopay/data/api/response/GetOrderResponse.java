package vn.com.vng.zalopay.data.api.response;

/**
 * Created by Longlv on 5/8/16.
 */
public class GetOrderResponse extends BaseResponse {
    public String apptransid;
    public String appuser;
    public String item;
    public String amount;
    public String description;
    public String payoption;
    public Mac mac;
    public AppInfo appinfo;

    public class Mac {
        public String orderinfo;
        public String mac;
    }

    public  class  AppInfo {
        public String appname;
        public String logourl;
        public String status;
    }
}
