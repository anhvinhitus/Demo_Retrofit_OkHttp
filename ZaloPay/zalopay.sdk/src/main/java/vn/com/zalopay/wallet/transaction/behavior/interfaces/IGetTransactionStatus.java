package vn.com.zalopay.wallet.transaction.behavior.interfaces;

import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;

public interface IGetTransactionStatus {
    void getStatus(AbstractWorkFlow pAdapter, String pZmpTransID, boolean pCheckData, String pMessage);
}
