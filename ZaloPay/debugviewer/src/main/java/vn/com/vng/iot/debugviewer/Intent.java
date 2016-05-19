package vn.com.vng.iot.debugviewer;

import android.content.Context;

class Intent {
	static final String START_INTENT = "vn.com.vng.iot.debugviewer.intent.START";
	static final String SAVE_INTENT = "vn.com.vng.iot.debugviewer.intent.SAVE";
	static final String SHARE_INTENT = "vn.com.vng.iot.debugviewer.intent.SHARE";
	public static final String DEBUGVIEWER_INTENT = "vn.com.vng.iot.debugviewer.intent.DEBUG";
	
	static final String EXTRA_FILTER = "FILTER";
	static final String EXTRA_LEVEL = "LEVEL";
	static final String EXTRA_FREQUENCY = "FREQUENCY";
	static final String EXTRA_START_RECORD = "START_WRITE";
	static final String EXTRA_STOP_RECORD = "STOP_WRITE";
	
	static void handleExtras(Context context, android.content.Intent intent) {
		Prefs prefs = new Prefs(context);
		String filter = intent.getStringExtra(EXTRA_FILTER);
		if (filter != null) {
			prefs.setFilter(filter);
		}
		String l = intent.getStringExtra(EXTRA_LEVEL);
		if (l != null) {
			Level level = Level.valueOf(l);
			prefs.setLevel(level);
		}
	}
}
