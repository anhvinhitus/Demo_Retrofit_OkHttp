package vn.com.vng.iot.debugviewer;

import java.util.HashMap;

import android.content.Context;

enum Buffer {
	MAIN("main", R.string.main_title),
	EVENTS("events", R.string.events_title),
	RADIO("radio", R.string.radio_title);
	
	private static Buffer[] byOrder = new Buffer[3];

	static {
		byOrder[0] = MAIN;
		byOrder[1] = EVENTS;
		byOrder[2] = RADIO;
	}
	
	private static final HashMap<String,Buffer> VALUE_MAP = new HashMap<String,Buffer>();
	
	static {
		VALUE_MAP.put(MAIN.mValue, MAIN); 
		VALUE_MAP.put(EVENTS.mValue, EVENTS); 
		VALUE_MAP.put(RADIO.mValue, RADIO); 
	}
		
	private String mValue;
	private int mTitleId;
	
	private Buffer(String value, int titleId) {
		mValue = value;
		mTitleId = titleId;
	}
	
	public String getTitle(Context context) {
		return context.getResources().getString(mTitleId);
	}	
	
	public static Buffer byValue(String value) {
		return VALUE_MAP.get(value);
	}
	
	public static Buffer getByOrder(int order) {
		return byOrder[order];
	}
	
	public String getValue() {
		return mValue;
	}
}
