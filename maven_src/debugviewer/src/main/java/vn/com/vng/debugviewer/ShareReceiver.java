package vn.com.vng.debugviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class ShareReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d("alogcat", "received intent for share");

		vn.com.vng.debugviewer.Intent.handleExtras(context, intent);

		Lock.acquire(context);

		Intent svcIntent = new Intent(context, ShareService.class);
		context.startService(svcIntent);
	}

}
