package vn.com.zalopay.wallet.business.transaction.behavior.getstatus;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.IGetTransactionStatus;
import vn.com.zalopay.wallet.datasource.task.BaseTask;
import vn.com.zalopay.wallet.datasource.task.getstatus.GetStatus;

public class CGetPaymentStatus implements IGetTransactionStatus {
    @Override
    public void getStatus(AdapterBase pAdapter, String pZmpTransID, boolean pCheckData, String pMessage) {
        BaseTask getStatusTask = new GetStatus(pAdapter, pZmpTransID, pCheckData, pMessage);
        getStatusTask.makeRequest();
    }
}
