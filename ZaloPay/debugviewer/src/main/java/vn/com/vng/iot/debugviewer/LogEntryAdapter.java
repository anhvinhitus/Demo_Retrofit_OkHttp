package vn.com.vng.iot.debugviewer;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class LogEntryAdapter extends ArrayAdapter<LogEntry> {
	private Activity mActivity;
	private List<LogEntry> entries;
	private Prefs mPrefs;

	public LogEntryAdapter(Activity activity, int resourceId,
			List<LogEntry> entries) {
		super(activity, resourceId, entries);
		this.mActivity = activity;
		this.entries = entries;
		this.mPrefs = new Prefs(activity);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LogEntry entry = entries.get(position);
		TextView tv;
		if (convertView == null) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			tv = (TextView) inflater.inflate(R.layout.entry, null);
		} else {
			tv = (TextView) convertView;
		}

        tv.setTextColor(entry.getLevel().getColor());

		String text = entry.getText();
		int begin = text.indexOf("{");
		int end = text.lastIndexOf("}");
		boolean plainText = true;

		if (begin >= 0 && end >=0 && begin < end) {
            String jsonString = text.substring(begin, end + 1);
			try {
                IFragmentHighlighter highlighter = new JSONFragmentHighlighter();
                SpannableString html = highlighter.apply(jsonString);
                tv.setText(html, TextView.BufferType.SPANNABLE);
                plainText = false;
			} catch (RuntimeException e) {
                plainText = true;
			}
		}

        if (plainText) {
            tv.setText(entry.getText());
        }
		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mPrefs.getTextsize()
				.getValue());

		return tv;
	}

	public void remove(int position) {
		LogEntry entry = entries.get(position);
		remove(entry);
	}

	public boolean areAllItemsEnabled() {
		return false;
	}

	public boolean isEnabled(int position) {
		return false;
	}

	public LogEntry get(int position) {
		return entries.get(position);
	}
	
	public List<LogEntry> getEntries() {
		return Collections.unmodifiableList(entries);
	}
}

