package vn.com.vng.zalopay.data.ws.connection;

/**
 * Created by huuhoa on 8/15/16.
 * Provide configuration, event handlers
 */
interface TimerListener {
    /**
     * Get amount of time in milliseconds between subsequent executions.
     * @return amount of time in milliseconds between subsequent executions.
     */
    int period();

    /**
     * Get amount of time in milliseconds before first execution.
     * @return amount of time in milliseconds before first execution.
     */
    int delay();

    /**
     * called when timer ticked
     */
    void onEvent();
}
