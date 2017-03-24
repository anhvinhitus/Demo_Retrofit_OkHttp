package vn.com.vng.zalopay.data.cache.model;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "APP_RESOURCE_GD".
 */
@Entity
public class AppResourceGD {

    @Unique
    public long appid;
    public String appname;
    public Long needdownloadrs;
    public String imageurl;
    public String jsurl;
    public Long status;
    public String checksum;
    public Long apptype;
    public String weburl;
    public String iconname;
    public String iconcolor;
    public Long sortOrder;
    public Long stateDownload;
    public Long timeDownload;
    public Long numRetry;


}