package vn.com.vng.debugviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import android.content.*;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class Logcat {
	private static final long CAT_DELAY = 1;

	private Level mLevel = null;
	private String mFilter = null;
	private Pattern mFilterPattern = null;
	private boolean mRunning = false;
	private BufferedReader mReader = null;
	private boolean mIsFilterPattern;
	private Handler mHandler;
	private Buffer mBuffer;
	private Process logcatProc;
	private Context mContext;
	private ArrayList<String> mLogCache = new ArrayList<String>();
	private boolean mPlay = true;
	private long lastCat = -1;
	private IntentFilter mIntentFilter = null;

	private Runnable catRunner = new Runnable() {

		@Override
		public void run() {
			if (!mPlay) {
				return;
			}
			long now = System.currentTimeMillis();
			if (now < lastCat + CAT_DELAY) {
				return;
			}
			lastCat = now;
			cat();
		}
	};
	private ScheduledExecutorService EX;

	Format mFormat;

	public Logcat(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;

		Prefs prefs = new Prefs(mContext);

		mLevel = prefs.getLevel();
		mIsFilterPattern = prefs.isFilterPattern();
		mFilter = prefs.getFilter();
		mFilterPattern = prefs.getFilterPattern();
		mFormat = prefs.getFormat();
		mBuffer = prefs.getBuffer();

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Constants.ACTION_POST_LOG);
	}

    private void addMessage(String message) {
        if (mIsFilterPattern) {
            if (mFilterPattern != null
                    && !mFilterPattern.matcher(message).find()) {
                return;
            }
        } else {
            if (mFilter != null
                    && !message.toLowerCase().contains(
                    mFilter.toLowerCase())) {
                return;
            }
        }
        synchronized (mLogCache) {
            mLogCache.add(message);
        }
    }

	public void start() {
		stop();

		mRunning = true;

		EX = Executors.newScheduledThreadPool(1);
		EX.scheduleAtFixedRate(catRunner, CAT_DELAY, CAT_DELAY,
				TimeUnit.SECONDS);

		try {
            DebugViewer.instance().registerMessageListener(new LogCacheStorage.ILogListener() {
                @Override
                public void handleMessage(String message) {
                    Log.d("CAT", "Receive message: " + message);
                    addMessage(message);
                }

                @Override
                public void handleClear() {
                    synchronized (mLogCache) {
                        mLogCache.clear();
                    }
                    Message m = Message.obtain(mHandler, LogActivity.CLEAR_WHAT);
                    mHandler.sendMessage(m);
                }
            });

            ArrayList<String> messages = DebugViewer.instance().getLogs();
            if (messages != null && !messages.isEmpty()) {
                for (String msg : messages) {
                    addMessage(msg);
                }
            }

			Message m = Message.obtain(mHandler, LogActivity.CLEAR_WHAT);
			mHandler.sendMessage(m);

			while (mRunning) {
				if (!mRunning) {
					break;
				}
                Thread.sleep(200);
			}
        } catch (InterruptedException ex) {
            return;
        } finally {
            DebugViewer.instance().unregisterMessageListener();

			if (logcatProc != null) {
				logcatProc.destroy();
				logcatProc = null;
			}
			if (mReader != null) {
				try {
					mReader.close();
					mReader = null;
				} catch (IOException e) {
					Log.e("alogcat", "error closing stream", e);
				}
			}
		}
	}

	private void cat() {
		Message m;

		if (mLogCache.size() > 0) {
			synchronized (mLogCache) {
				if (mLogCache.size() > 0) {
					m = Message.obtain(mHandler, LogActivity.CAT_WHAT);
					m.obj = mLogCache.clone();
					mLogCache.clear();
					mHandler.sendMessage(m);					
				}
			}
		}
	}

	public void stop() {
		// Log.d("alogcat", "stopping ...");
		mRunning = false;

		if (EX != null && !EX.isShutdown()) {
			EX.shutdown();
			EX = null;
		}
	}

	public boolean isRunning() {
		return mRunning;
	}

	public boolean isPlay() {
		return mPlay;
	}

	public void setPlay(boolean play) {
		mPlay = play;
	}

}
