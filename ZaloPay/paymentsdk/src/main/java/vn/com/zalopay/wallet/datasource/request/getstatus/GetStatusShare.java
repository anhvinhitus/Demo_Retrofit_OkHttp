package vn.com.zalopay.wallet.datasource.request.getstatus;


import java.util.HashMap;

import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.objectmanager.SingletonBase;
import vn.com.zalopay.wallet.listener.IDataSourceListener;

public class GetStatusShare extends SingletonBase implements IGetPaymentStatus {
    public static GetStatusShare _object;
    protected IGetPaymentStatus mIGetPaymentStatus;

    public GetStatusShare() {
        super();

        if (GlobalData.isLinkCardChannel()) {
            mIGetPaymentStatus = new GetMapCardStatus();
        } else {
            mIGetPaymentStatus = new GetPaymentStatus();
        }
    }

    public static GetStatusShare shared() {
        if (GetStatusShare._object == null) {
            GetStatusShare._object = new GetStatusShare();
        }

        return GetStatusShare._object;
    }

    @Override
    public void onGetStatus(IDataSourceListener pListener, HashMap<String, String> pParamsRequest, boolean pIsRetry) {
        if (mIGetPaymentStatus != null) {
            mIGetPaymentStatus.onGetStatus(pListener, pParamsRequest, pIsRetry);
        }
    }

    @Override
    public void onPrepareParamsGetStatus(HashMap<String, String> pParamsRequest, String pTransactionId) throws Exception {
        if (mIGetPaymentStatus != null) {
            mIGetPaymentStatus.onPrepareParamsGetStatus(pParamsRequest, pTransactionId);
        }
    }
}
