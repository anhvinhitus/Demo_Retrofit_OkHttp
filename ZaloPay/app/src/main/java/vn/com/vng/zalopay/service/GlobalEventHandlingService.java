package vn.com.vng.zalopay.service;

import android.support.annotation.Nullable;

/**
 * Created by huuhoa on 6/11/16.
 * Interface for handing global event
 */
public interface GlobalEventHandlingService extends Thread.UncaughtExceptionHandler {
    class Message {
        public final int messageType;
        public final String title;
        public final String content;

        public Message(int messageTitle, String title, String content) {
            this.title = title;
            this.content = content;
            this.messageType = messageTitle;
        }
    }

    void enqueueMessage(int messageType, String title, String body);

    //! Show message box at home
    @Nullable
    Message popMessage();
}
