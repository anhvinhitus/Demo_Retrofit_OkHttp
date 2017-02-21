package vn.com.vng.webapp.framework;

import org.json.JSONObject;

/**
 * Created by huuhoa on 2/18/17.
 * Interface that represent the native message handler module
 */
public interface NativeModule {
    /**
     * JS-bridge module will call processMessage to handle the data passed from webapp to native
     * @param messageName message name
     * @param messageType message type. Default value is call
     * @param data message data
     * @param promise After finished processing message, handler call the promise method to return
     *                result to webapp
     */
    void processMessage(String messageName, String messageType, JSONObject data, Promise promise);

    /**
     * Return list of message name that the native module can handle
     */
    String[] canProcessMessages();
}
