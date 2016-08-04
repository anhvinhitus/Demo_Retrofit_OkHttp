package vn.com.vng.debugviewer;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

public enum Level {
	V(Log.VERBOSE, "#121212", R.string.verbose_title),
	D(Log.DEBUG, "#00006C", R.string.debug_title),
	I(Log.INFO, "#20831B", R.string.info_title),
	W(Log.WARN, "#FD7916", R.string.warn_title),
	E(Log.ERROR, "#FD0010", R.string.error_title),
	F(Log.ASSERT, "#ff0066", R.string.fatal_title);

	private String mHexColor;
	private int mColor;
	private int mValue;
	private int mTitleId;

	Level(int value, String hexColor, int titleId) {
		mValue = value;
		mHexColor = hexColor;
		mColor = Color.parseColor(hexColor);
		mTitleId = titleId;
	}

	public String getHexColor() {
		return mHexColor;
	}
	
	public int getColor() {
		return mColor;
	}

	public int getValue() {
		return mValue;
	}

	public String getTitle(Context context) {
		return context.getResources().getString(mTitleId);
	}
}
