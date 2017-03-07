package vn.com.zalopay.wallet.business.transaction.behavior.interfaces;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;

public interface IGetTransactionStatus {
    void getStatus(AdapterBase pAdapter, String pZmpTransID, boolean pCheckData, String pMessage);
}
