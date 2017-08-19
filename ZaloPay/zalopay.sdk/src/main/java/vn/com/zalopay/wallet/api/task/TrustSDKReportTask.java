package vn.com.zalopay.wallet.api.task;


import timber.log.Timber;
import vn.com.zalopay.utility.ConnectionUtil;
import vn.com.zalopay.wallet.api.implement.SDKReportImpl;
import vn.com.zalopay.wallet.configure.GlobalData;

/***
 * always log to server
 * no check current connect
 */
public class TrustSDKReportTask extends SDKReportTask {
    public TrustSDKReportTask(String... pParams) {
        super(null, pParams);
    }

    public TrustSDKReportTask(int pExInfo, String... pParams) {
        super(null, pExInfo, pParams);
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
            Timber.d("can not send log report error because networking is offline");
            return;
        }
        newDataRepository().setTask(this).postData(new SDKReportImpl(), getDataParams());
    }
}
