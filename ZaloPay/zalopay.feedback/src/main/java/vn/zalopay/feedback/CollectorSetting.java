package vn.zalopay.feedback;

/**
 * Created by huuhoa on 12/15/16.
 * Settings for data collector
 */

public class CollectorSetting {
    /**
     * Creator of data collector determine whether this collector is hidden to user or not.
     * Hidden collector will automatically collect data without User's acknowledgement.
     */
    public boolean userVisibility;

    /**
     * User's friendly name of the data collector
     */
    public String displayName;

    /**
     * Key name for data encoding
     */
    public String dataKeyName;
}
