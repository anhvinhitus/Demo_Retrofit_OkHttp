package vn.com.vng.zalopay.data.zfriend;

import vn.com.vng.zalopay.data.cache.model.ZaloFriendGDDao;

/**
 * Created by AnhHieu on 10/12/16.
 * *
 */

public interface ColumnIndex {
    int Id = ZaloFriendGDDao.Properties.Id.ordinal;
    int UserName = ZaloFriendGDDao.Properties.UserName.ordinal;
    int DisplayName = ZaloFriendGDDao.Properties.DisplayName.ordinal;
    int Avatar = ZaloFriendGDDao.Properties.Avatar.ordinal;
    int UserGender = ZaloFriendGDDao.Properties.UserGender.ordinal;
    int Birthday = ZaloFriendGDDao.Properties.Birthday.ordinal;
    int UsingApp = ZaloFriendGDDao.Properties.UsingApp.ordinal;
    int Fulltextsearch = ZaloFriendGDDao.Properties.Fulltextsearch.ordinal;
}
