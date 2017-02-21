package vn.com.vng.webapp.framework;

import org.json.JSONObject;

/**
 * Created by huuhoa on 2/8/17.
 * WebMessage to hold data for communication between native and webview
 */

public class WebMessage {
    /**
     * Unique id for tracking request-callback session
     */
    public String messageId;

    /**
     * Name of request function
     */
    public String functionName;

    /**
     * Data as parameters/response for function
     */
    public JSONObject data;

    /**
     * Type of the message
     */
    public String messageType;

    /**
     * Keep callback function flag
     */
    public boolean keepCallback;
}
