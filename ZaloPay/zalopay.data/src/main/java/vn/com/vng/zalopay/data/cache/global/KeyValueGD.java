package vn.com.vng.zalopay.data.cache.global;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "KEY_VALUE_GD".
 */
@Entity
public class KeyValueGD {

    @Id
    @NotNull
    @Unique
    public String key;
    public String value;


}