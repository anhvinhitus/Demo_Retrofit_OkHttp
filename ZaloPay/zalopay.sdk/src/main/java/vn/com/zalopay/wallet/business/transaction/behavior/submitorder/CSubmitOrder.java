package vn.com.zalopay.wallet.business.transaction.behavior.submitorder;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.business.transaction.behavior.interfaces.IDoSubmit;
import vn.com.zalopay.wallet.datasource.request.SubmitOrder;

public class CSubmitOrder implements IDoSubmit {
    @Override
    public void doSubmit(AdapterBase pAdapter) {
        (new SubmitOrder(pAdapter)).makeRequest();
    }
}
