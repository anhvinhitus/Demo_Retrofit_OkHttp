package vn.com.vng.zalopay.data.cache;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.AppEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface SqlitePlatformScope {
    void writeCards(List<CardEntity> listCard);

    void write(CardEntity card);

    void writeApplications(List<AppEntity> list);

    void write(AppEntity appEntity);

    List<AppEntity> getAllAppEntity();

    List<List<CardEntity>> getAllCardEntity();
}
