package vn.com.vng.zalopay;

import java.util.ArrayList;

import vn.com.zalopay.wallet.business.entity.user.UserProfile;

/**
 * Created by cpu10985 on 02/06/2016.
 */
public class TokenModel {
    public String accesstoken;
    public String userid;

    public int profilelevel;

    public ArrayList<UserProfile> profilelevelpermisssion;
}
