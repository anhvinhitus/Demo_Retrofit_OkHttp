package vn.com.vng.zalopay.data.cache.model;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

import vn.com.vng.zalopay.domain.model.IPersistentObject;

/**
 * Entity mapped to table "ZALO_FRIEND_GD".
 */
public class ZaloFriendGD implements IPersistentObject {

    private Long id;
    private String userName;
    private String displayName;
    private String avatar;
    private Integer userGender;
    private String birthday;
    private Boolean usingApp;
    private String fulltextsearch;

    public ZaloFriendGD() {
    }

    public ZaloFriendGD(Long id) {
        this.id = id;
    }

    public ZaloFriendGD(Long id, String userName, String displayName, String avatar, Integer userGender, String birthday, Boolean usingApp, String fulltextsearch) {
        this.id = id;
        this.userName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
        this.userGender = userGender;
        this.birthday = birthday;
        this.usingApp = usingApp;
        this.fulltextsearch = fulltextsearch;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getUserGender() {
        return userGender;
    }

    public void setUserGender(Integer userGender) {
        this.userGender = userGender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public Boolean getUsingApp() {
        return usingApp;
    }

    public void setUsingApp(Boolean usingApp) {
        this.usingApp = usingApp;
    }

    public String getFulltextsearch() {
        return fulltextsearch;
    }

    public void setFulltextsearch(String fulltextsearch) {
        this.fulltextsearch = fulltextsearch;
    }

}
