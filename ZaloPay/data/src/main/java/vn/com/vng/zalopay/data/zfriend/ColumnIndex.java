package vn.com.vng.zalopay.data.zfriend;

import vn.com.vng.zalopay.data.cache.model.ZaloFriendGDDao;
import vn.com.vng.zalopay.data.cache.model.ZaloPayProfileGDDao;

/**
 * Created by AnhHieu on 10/12/16.
 * *
 */

public interface ColumnIndex {
    int ID = ZaloFriendGDDao.Properties.ZaloId.ordinal;
    int USER_NAME = ZaloFriendGDDao.Properties.UserName.ordinal;
    int DISPLAY_NAME = ZaloFriendGDDao.Properties.DisplayName.ordinal;
    int AVATAR = ZaloFriendGDDao.Properties.Avatar.ordinal;
    int USING_APP = ZaloFriendGDDao.Properties.UsingApp.ordinal;
    String ALIAS_DISPLAY_NAME = "ALIAS_DISPLAY_NAME";
    String ALIAS_FULL_TEXT_SEARCH = "ALIAS_FULLTEXTSEARCH";
    String ZALOPAY_ID = ZaloPayProfileGDDao.Properties.ZaloPayId.columnName;
    String STATUS = ZaloPayProfileGDDao.Properties.Status.columnName;
}
