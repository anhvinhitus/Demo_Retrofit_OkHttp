package vn.com.zalopay.wallet.transaction.behavior.submitorder;

import vn.com.zalopay.wallet.business.channel.base.AdapterBase;
import vn.com.zalopay.wallet.transaction.behavior.interfaces.IDoSubmit;
import vn.com.zalopay.wallet.api.task.VerifyMapCardTask;

public class CVerifyCardMap implements IDoSubmit {

    @Override
    public void doSubmit(AdapterBase pAdapter) {
        new VerifyMapCardTask(pAdapter).makeRequest();
    }
}
