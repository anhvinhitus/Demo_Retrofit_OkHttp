package vn.com.vng.zalopay.event;

/**
 * Created by hieuvm on 4/21/17.
 * *
 */

public class UploadFileLogEvent {
    public String filePath;

    public UploadFileLogEvent(String filePath) {
        this.filePath = filePath;
    }
}
