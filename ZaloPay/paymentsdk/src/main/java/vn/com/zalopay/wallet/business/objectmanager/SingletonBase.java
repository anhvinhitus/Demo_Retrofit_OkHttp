package vn.com.zalopay.wallet.business.objectmanager;

public abstract class SingletonBase {

    /**
     * Register this singleton at the manager
     */
    public SingletonBase() {
        SingletonLifeCircleManager.register(this.getClass());
    }
}
