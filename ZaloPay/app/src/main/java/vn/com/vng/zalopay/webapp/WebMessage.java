package vn.com.vng.zalopay.webapp;

import org.json.JSONObject;

/**
 * Created by huuhoa on 2/8/17.
 * WebMessage to hold data for communication between native and webview
 */

class WebMessage {
    /**
     * Unique id for tracking request-callback session
     */
    String messageId;

    /**
     * Name of request function
     */
    String functionName;

    /**
     * Data as parameters/response for function
     */
    JSONObject data;

    /**
     * Type of the message
     */
    String messageType;

    /**
     * Keep callback function flag
     */
    boolean keepCallback;
}
