package vn.com.zalopay.wallet.datasource.request;


import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.datasource.DataRepository;
import vn.com.zalopay.wallet.datasource.implement.SDKReportImpl;
import vn.com.zalopay.wallet.utils.ConnectionUtil;
import vn.com.zalopay.wallet.utils.Log;

/***
 * alway log to server
 * no check current connect
 */
public class TrustSDKReport extends SDKReport {

    public TrustSDKReport(String... pParams) {
        super(pParams);
    }
    public TrustSDKReport(int pExInfo, String... pParams) {
        super(pExInfo,pParams);
    }
    /***
     * @param pCode
     * @param pParams [transid,exception,bankcode]
     */
    public static void makeTrustReportError(int pCode, String... pParams) {
        BaseRequest baseRequest = new TrustSDKReport(pCode, pParams);
        baseRequest.makeRequest();
    }

    /***
     * overlap function
     *
     * @param pParams [transid,exception,bankcode]
     */
    public static void makeReportError(String... pParams) {
        BaseRequest baseRequest = new TrustSDKReport(pParams);
        baseRequest.makeRequest();
    }

    @Override
    protected void doRequest() {
        try {
            if (!ConnectionUtil.isOnline(GlobalData.getAppContext())) {
                Log.e("makeReportError", "====cant send log report error because networking is offline");
                return;
            }
            Log.d(this,"TrustSDKReport.doRequest");
            DataRepository.newInstance().setDataSourceListener(getDataSourceListener()).pushData(new SDKReportImpl(), getDataParams());
        } catch (Exception ex) {
            onRequestFail(null);
        }
    }
}
