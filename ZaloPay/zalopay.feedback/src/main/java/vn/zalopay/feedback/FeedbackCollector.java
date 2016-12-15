package vn.zalopay.feedback;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import timber.log.Timber;

/**
 * Created by huuhoa on 12/15/16.
 * Collector coordinate
 */

public class FeedbackCollector {
    private final List<IFeedbackCollector> mCollectors = new ArrayList<>();
    /**
     * Install new data collector
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
    public void startCollectors() {
        DataCollectorAsyncTask task = new DataCollectorAsyncTask();
        task.execute();

    }

    /**
     * Show feedback dialog to user
     */
    public void showFeedbackDialog() {

    }

    private class DataCollectorAsyncTask extends AsyncTask<Void, Void, JSONArray> {

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
        protected JSONArray doInBackground(Void[] params) {
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
                    } catch (JSONException e) {
                        continue;
                    }
                }
            }
            return array;
        }

        /**
         * <p>Applications should preferably override {@link #onCancelled(Object)}.
         * This method is invoked by the default implementation of
         * {@link #onCancelled(Object)}.</p>
         * <p>
         * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
         * {@link #doInBackground(Object[])} has finished.</p>
         *
         * @see #onCancelled(Object)
         * @see #cancel(boolean)
         * @see #isCancelled()
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param o The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(JSONArray o) {
            try {
                JSONObject object = new JSONObject();
                object.put("data", o);
                Timber.d("Data collected: %s", object);
            } catch (JSONException e) {

            }
        }

        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}
