package vn.com.zalopay.game.businnesslogic.base;

public abstract class AppGameBaseSingleton {

	/**
	 * Register this singleton at the manager
	 */
	public AppGameBaseSingleton() {
		AppGameSingletonLifeCircle.register(this.getClass());
	}
}
