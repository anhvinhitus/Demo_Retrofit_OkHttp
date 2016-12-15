package vn.zalopay.feedback;

/**
 * Created by huuhoa on 12/15/16.
 * Interface for defining data collector
 */

public interface IFeedbackCollector {
    /**
     * Get pre-config settings for data collector
     */
    CollectorSetting getSetting();
}
