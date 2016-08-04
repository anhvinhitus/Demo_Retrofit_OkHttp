package vn.com.vng.debugviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class SaveReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d("alogcat", "received intent for save");

		vn.com.vng.debugviewer.Intent.handleExtras(context, intent);

		Lock.acquire(context);

		Intent svcIntent = new Intent(context, SaveService.class);
		context.startService(svcIntent);
	}
}
