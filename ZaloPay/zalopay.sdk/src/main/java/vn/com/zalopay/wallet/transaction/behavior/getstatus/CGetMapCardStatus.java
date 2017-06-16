package vn.com.zalopay.wallet.transaction.behavior.getstatus;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IGetTransactionStatus;
import vn.com.zalopay.wallet.api.task.BaseTask;
import vn.com.zalopay.wallet.api.task.getstatus.GetStatus;

public class CGetMapCardStatus implements IGetTransactionStatus {
    @Override
    public void getStatus(AdapterBase pAdapter, String pZmpTransID, boolean pCheckData, String pMessage) {
        BaseTask getStatusTask = new GetStatus(pAdapter, pZmpTransID, pCheckData, pMessage);
        getStatusTask.makeRequest();
    }
}
