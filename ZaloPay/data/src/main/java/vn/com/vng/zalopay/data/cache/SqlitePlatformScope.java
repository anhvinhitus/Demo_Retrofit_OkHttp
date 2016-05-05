package vn.com.vng.zalopay.data.cache;

import java.util.List;

import vn.com.vng.zalopay.data.api.entity.CardEntity;

/**
 * Created by AnhHieu on 4/28/16.
 */
public interface SqlitePlatformScope {
    void writeCards(List<CardEntity> listCard);

    void write(CardEntity card);

    List<List<CardEntity>> getAllCardEntity();
}
