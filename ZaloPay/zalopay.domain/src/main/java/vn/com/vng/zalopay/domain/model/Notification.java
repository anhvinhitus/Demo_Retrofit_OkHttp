package vn.com.vng.zalopay.domain.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lytm on 10/08/2017.
 */

public class Notification {

    @SerializedName("notification_types_vibrate")
    public List<Integer> mVibrateNotificationType;

}
