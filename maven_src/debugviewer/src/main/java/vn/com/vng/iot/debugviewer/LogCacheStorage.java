package vn.com.vng.iot.debugviewer;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by huuhoa on 12/26/15.
 */
class LogCacheStorage {
    public void clear() {
        logEntries.clear();
        if (mListener != null) {
            mListener.handleClear();
        }
    }

    public interface ILogListener {
        void handleMessage(String message);
        void handleClear();
    }

    private class LogEntry {
        private int level = Log.VERBOSE;
        private String text = null;
        private String tag = null;
        private Date timeStamp;

        public LogEntry(int level, String tag, String text) {
            this.level = level;
            this.tag = tag;
            this.text = text;
            timeStamp = new Date();
        }

        public String toString() {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd hh:mm:s.SSS");
            return String.format("%s %s/%s: %s",
                    format.format(timeStamp), fromLevel(), tag, text);
        }

        private String fromLevel() {
            if (level == Log.VERBOSE) {
                return "V";
            } else if (level == Log.DEBUG) {
                return "D";
            } else if (level == Log.INFO) {
                return "I";
            } else if (level == Log.WARN) {
                return "W";
            } else if (level == Log.ERROR) {
                return "E";
            } else if (level == Log.ASSERT) {
                return "F";
            }
            return "D";
        }
    }

    private List<LogEntry> logEntries = new ArrayList<>();

    private ILogListener mListener = null;

    public void setListener(ILogListener mListener) {
        this.mListener = mListener;
    }

    public ArrayList<String> getLogs() {
        android.content.Intent in = new android.content.Intent();
        in.setAction(Constants.ACTION_POST_LOG);
        ArrayList<String> entries = new ArrayList<>();
        for (LogEntry entry : logEntries) {
            entries.add(entry.toString());
        }

        return entries;
    }

    public void postLog(int level, String tag, String text) {
        LogEntry entry = new LogEntry(
                level,
                tag,
                text
        );
        logEntries.add(entry);

        if (mListener != null) {
            mListener.handleMessage(entry.toString());
        }
//        android.content.Intent in = new android.content.Intent();
//        in.setAction(Constants.ACTION_POST_LOG);
//        in.putExtra("message", entry.toString());
//        sendBroadcast(in);
    }
}
