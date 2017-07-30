package vn.com.vng.zalopay.tracker;

/**
 * Created by huuhoa on 7/30/17.
 *
 * Data class to store information for tracking Activity events
 */

public class ActivityTracker {
    public final String activityName;
    public final int launchEventId;
    public final int backEventId;

    public ActivityTracker(String activityName, int launchEventId, int backEventId) {
        String activityNameTemp = activityName;
        if (activityName == null) {
            activityNameTemp = ""; // convert from null to empty string
        }

        this.activityName = activityNameTemp;
        this.launchEventId = launchEventId;
        this.backEventId = backEventId;
    }
}
