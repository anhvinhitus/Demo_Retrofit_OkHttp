package vn.com.zalopay.wallet.datasource.task;


import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.datasource.implement.SDKReportImpl;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.Log;

/***
 * always log to server
 * no check current connect
 */
public class TrustSDKReportTask extends SDKReportTask {
    public TrustSDKReportTask(String... pParams) {
        super(pParams);
    }

    public TrustSDKReportTask(int pExInfo, String... pParams) {
        super(pExInfo, pParams);
    }

    /***
     * @param pCode
     * @param pParams [transid,exception,bankcode]
     */
    public static void makeTrustReportError(int pCode, String... pParams) {
        BaseTask baseRequest = new TrustSDKReportTask(pCode, pParams);
        baseRequest.makeRequest();
    }

    /***
     * overlap function
     * @param pParams [transid,exception,bankcode]
     */
    public static void makeReportError(String... pParams) {
        BaseTask baseRequest = new TrustSDKReportTask(pParams);
        baseRequest.makeRequest();
    }

    @Override
    protected void doRequest() {
        if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
            Log.d(this, "can not send log report error because networking is offline");
            return;
        }
        newDataRepository().setTask(this).postData(new SDKReportImpl(), getDataParams());
    }
}
