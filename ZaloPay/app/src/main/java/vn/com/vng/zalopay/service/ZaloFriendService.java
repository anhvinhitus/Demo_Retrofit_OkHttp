package vn.com.vng.zalopay.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by longlv on 20/06/2016.
 */
public class ZaloFriendService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ZaloFriendService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
