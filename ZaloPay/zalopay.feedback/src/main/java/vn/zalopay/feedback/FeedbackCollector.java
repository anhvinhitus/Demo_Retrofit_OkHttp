package vn.zalopay.feedback;

import java.util.ArrayList;
import java.util.List;

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
     * Show feedback dialog to user
     */
    public void showFeedbackDialog() {

    }
}
