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
        public final int iTitle;
        public final int iContent;

        public Message(int messageTitle, String title, String content) {
            this.title = title;
            this.content = content;
            this.messageType = messageTitle;
            this.iTitle = -1;
            this.iContent = -1;
        }

        public Message(int messageTitle, int title, int content) {
            this.iTitle = title;
            this.iContent = content;
            this.messageType = messageTitle;
            this.title = null;
            this.content = null;
        }
    }

    void enqueueMessage(int messageType, String title, String body);
    void enqueueMessageAtLogin(int messageType, String title, String body);
    void enqueueMessageAtLogin(int messageType, int title, int body);

    //! Show message box at home
    @Nullable
    Message popMessage();

    //! Show message box at login
    Message popMessageAtLogin();
}
