package vn.com.vng.zalopay.data.cache;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.AppInfoEntity;

/**
 * Created by AnhHieu on 5/4/16.
 */
public interface SqlAppListScope {

    void write(AppInfoEntity entity);

    void write(List<AppInfoEntity> entityList);
}
