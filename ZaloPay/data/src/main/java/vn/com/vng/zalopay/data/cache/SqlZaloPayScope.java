package vn.com.vng.zalopay.data.cache;

import java.util.List;

import vn.com.vng.zalopay.data.cache.model.TransferRecent;

/**
 * Created by AnhHieu on 5/4/16.
 */
public interface SqlZaloPayScope extends SqlBaseScope {

    void writeTransferRecent(TransferRecent val);

    List<TransferRecent> listTransferRecent();

    List<TransferRecent> listTransferRecent(int limit);
}
