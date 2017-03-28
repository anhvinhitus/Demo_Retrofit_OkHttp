package vn.com.zalopay.wallet.datasource.task;

import java.util.HashMap;

import retrofit2.Call;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.ITask;

public class TPaymentTask extends TaskBase {
    private static TPaymentTask _object;
    protected ITask mTask;

    public TPaymentTask() {
        super();
    }

    public static TPaymentTask shareInstance() {
        if (TPaymentTask._object == null) {
            TPaymentTask._object = new TPaymentTask();
        }
        return TPaymentTask._object;
    }

    public static TPaymentTask newInstance() {
        return new TPaymentTask();
    }

    public ITask getTask() {
        return mTask;
    }

    public TPaymentTask setTask(ITask mTask) {
        this.mTask = mTask;
        return this;
    }

    @Override
    public Call doTask(IData pIData, HashMap<String, String> pParams) throws Exception {
        if (mTask == null) {
            throw new Exception("Can not run task because of mTRequest == null");
        }
        return mTask.doTask(pIData, pParams);
    }

    @Override
    public int getTaskEventId() {
        return 0;
    }
}
