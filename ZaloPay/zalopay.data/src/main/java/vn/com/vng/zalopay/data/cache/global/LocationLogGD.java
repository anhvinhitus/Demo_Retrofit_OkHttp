package vn.com.vng.zalopay.data.cache.global;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "LOCATION_LOG_GD".
 */
@Entity
public class LocationLogGD {

    @Id
    @Unique
    public long timeget;
    public Double latitude;
    public Double longitude;
    public String address;


}
