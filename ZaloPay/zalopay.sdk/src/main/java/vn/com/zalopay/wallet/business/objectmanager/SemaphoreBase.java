package vn.com.zalopay.wallet.business.objectmanager;

import java.util.concurrent.Semaphore;

/***
 * use semaphore to
 * control how your threads access
 * to same resource in sync way
 */
public abstract class SemaphoreBase extends SingletonBase {
    protected int PERMIT_POOL_SIZE = 1;
    protected Semaphore mSemaphore = new Semaphore(getPoolSize());

    public Semaphore getSemaphore() {
        return mSemaphore;
    }

    protected int getPoolSize() {
        return PERMIT_POOL_SIZE;
    }

    protected void setPoolSize(int pPoolSize) {
        this.PERMIT_POOL_SIZE = pPoolSize;
    }

    public void acquire() throws InterruptedException {
        mSemaphore.acquire();
    }

    public void acquire(int pPermitToAcquire) throws InterruptedException {
        mSemaphore.acquire(pPermitToAcquire);
    }

    public void release() {
        mSemaphore.release();
    }

    public void release(int pPermitToRelease) {
        mSemaphore.release(pPermitToRelease);
    }

    public int getAvailablePermits() {
        return mSemaphore.availablePermits();
    }
}
