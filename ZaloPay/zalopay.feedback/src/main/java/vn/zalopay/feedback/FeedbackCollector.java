package vn.zalopay.feedback;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import vn.zalopay.feedback.collectors.AppCollector;
import vn.zalopay.feedback.collectors.DeviceCollector;
import vn.zalopay.feedback.collectors.DynamicCollector;
import vn.zalopay.feedback.collectors.NetworkCollector;
import vn.zalopay.feedback.collectors.ScreenshotCollector;
import vn.zalopay.feedback.collectors.TransactionCollector;

/**
 * Created by huuhoa on 12/15/16.
 * Collector coordinate
 */

public class FeedbackCollector {

    private static FeedbackCollector _instance;

    public static FeedbackCollector instance() {
        if (_instance == null) {
            _instance = new FeedbackCollector();
        }
        return _instance;
    }

    private final List<IFeedbackCollector> mCollectors = new ArrayList<>();

    private DynamicCollector mDynamicCollector;
    private ScreenshotCollector mScreenshotCollector;
    private TransactionCollector mTransactionCollector;

    private FeedbackCollector() {
        mDynamicCollector = new DynamicCollector();
        mScreenshotCollector = new ScreenshotCollector();
        mTransactionCollector = new TransactionCollector();
    }

    private void installInternal() {
        installCollector(mDynamicCollector);
        installCollector(mScreenshotCollector);
        installCollector(mTransactionCollector);
    }

    /**
     * Install new data collector
     *
     * @param collector instance of new data collector
     */
    public void installCollector(IFeedbackCollector collector) {
        if (collector == null) {
            return;
        }

        synchronized (mCollectors) {
            if (mCollectors.contains(collector)) {
                return;
            }

            mCollectors.add(collector);
        }
    }

    /**
     * Remove an existing data collector
     *
     * @param collector data collector to be removed
     */
    public void removeCollector(IFeedbackCollector collector) {
        if (collector == null) {
            return;
        }

        synchronized (mCollectors) {
            mCollectors.remove(collector);
        }
    }

    /**
     * Start data collectors in the background thread
     */
    public void startCollectors(CollectorListener listener) {

        installInternal();

        DataCollectorAsyncTask task = new DataCollectorAsyncTask(mCollectors, listener);
        task.execute();
    }

    /**
     * Show feedback dialog to user
     */
    public void showFeedbackDialog() {

    }

    private static class DataCollectorAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private CollectorListener mListener;
        private final List<IFeedbackCollector> mCollectors;

        private DataCollectorAsyncTask(List<IFeedbackCollector> list,
                                       CollectorListener listener) {
            this.mListener = listener;
            this.mCollectors = list;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected JSONObject doInBackground(Void[] params) {
            JSONArray array = new JSONArray();
            synchronized (mCollectors) {
                for (IFeedbackCollector collector : mCollectors) {
                    try {
                        JSONObject item = new JSONObject();
                        item.put("name", collector.getSetting().dataKeyName);
                        JSONObject data = collector.doInBackground();
                        if (data != null) {
                            item.put("data", data);
                        }

                        array.put(item);
                    } catch (JSONException ignore) {
                    }
                }
            }

            JSONObject object = new JSONObject();
            try {
                object.put("data", array);
                Timber.d("Data collected: %s", object);
            } catch (JSONException ignore) {
            }

            return object;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param data The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(JSONObject data) {

            if (mListener != null) {
                mListener.onCollectorEnd(data);
            }
        }
    }

    public interface CollectorListener {
        void onCollectorEnd(JSONObject data);
    }

    public void setScreenShot(byte[] screenShot) {
        mScreenshotCollector.mScreenshot = screenShot;
    }

    public void setTransaction(String category, String transid, int errorcode, String errorMess) {
        mTransactionCollector.category = category;
        mTransactionCollector.transid = transid;
        mTransactionCollector.error_code = errorcode;
        mTransactionCollector.error_message = errorMess;
    }

    public void putDynamicInformation(String key, String value) {
        mDynamicCollector.put(key, value);
    }

    public void cleanUp() {
        for (IFeedbackCollector collector : mCollectors) {
            collector.cleanUp();
        }

        synchronized (mCollectors) {
            mCollectors.clear();
        }

    }

    public TransactionCollector getTransactionCollector() {
        return mTransactionCollector;
    }

    public byte[] getScreenshot() {
        return mScreenshotCollector.mScreenshot;
    }

    public void collectDeviceInformation(Context context, boolean app, boolean device, boolean network) {

        if (app) {
            installCollector(new AppCollector(context));
        }

        if (device) {
            installCollector(new DeviceCollector(context));
        }

        if (network) {
            installCollector(new NetworkCollector(context));
        }
    }
}
