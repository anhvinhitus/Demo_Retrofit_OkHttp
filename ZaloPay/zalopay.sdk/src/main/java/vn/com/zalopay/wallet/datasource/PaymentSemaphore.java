package vn.com.zalopay.wallet.datasource;


import vn.com.zalopay.wallet.business.objectmanager.SemaphoreBase;

public class PaymentSemaphore extends SemaphoreBase
{
    public PaymentSemaphore()
    {
        super();
    }

    @Override
    public void setPoolSize(int pPoolSize) {
        super.setPoolSize(pPoolSize);
    }
}
