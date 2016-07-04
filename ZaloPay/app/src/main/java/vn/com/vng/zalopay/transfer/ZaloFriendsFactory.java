package vn.com.vng.zalopay.transfer;

import vn.com.vng.zalopay.data.cache.SqlZaloPayScope;
import vn.com.vng.zalopay.data.cache.model.TransferRecent;

/**
 * Created by longlv on 13/06/2016.
 */
public class ZaloFriendsFactory {
    private SqlZaloPayScope sqlZaloPayScope;

    public ZaloFriendsFactory(SqlZaloPayScope sqlZaloPayScope) {
        this.sqlZaloPayScope = sqlZaloPayScope;
    }

    public void insertTransferRecent(vn.com.vng.zalopay.transfer.models.TransferRecent transferRecent) {
        TransferRecent transferEntity = convertTransactionRecent(transferRecent);
        sqlZaloPayScope.writeTransferRecent(transferEntity);
    }

    private TransferRecent convertTransactionRecent(vn.com.vng.zalopay.transfer.models.TransferRecent transferRecent) {
        if (transferRecent == null) {
            return null;
        }
        return new TransferRecent(transferRecent.getUserId(), transferRecent.getZaloPayId(), transferRecent.getUserName(), transferRecent.getDisplayName(), transferRecent.getAvatar(), transferRecent.getUserGender(), transferRecent.getBirthday(), transferRecent.isUsingApp(), transferRecent.getPhoneNumber(), transferRecent.getTransferType(), transferRecent.getAmount(), transferRecent.getMessage());
    }
}
