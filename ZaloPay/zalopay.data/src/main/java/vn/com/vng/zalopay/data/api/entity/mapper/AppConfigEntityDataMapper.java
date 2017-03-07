package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.AppResource;
import vn.com.vng.zalopay.domain.model.BankCard;

@Singleton
public class AppConfigEntityDataMapper {

    @Inject
    public AppConfigEntityDataMapper() {

    }

    public BankCard transform(CardEntity card) {
        BankCard bankCard = null;
        if (card != null) {
            bankCard = new BankCard(card.cardname, card.first6cardno, card.last4cardno, card.bankcode);
        }
        return bankCard;
    }

    public List<BankCard> transform(List<CardEntity> cards) {
        return Lists.transform(cards, this::transform);
    }

    public AppResource transform(AppResourceEntity entity) {
        AppResource appResource = null;
        if (entity != null) {
            appResource = new AppResource();
            appResource.checksum = entity.checksum;
            appResource.appname = entity.appname;
            appResource.status = entity.status;
            appResource.appid = entity.appid;
            appResource.appType = entity.apptype;
            appResource.webUrl = entity.weburl;
            appResource.urlImage = entity.imageurl;
            appResource.iconName = entity.iconName;
            appResource.iconColor = entity.iconColor;
        }
        return appResource;
    }

    public List<AppResource> transformAppResourceEntity(List<AppResourceEntity> entities) {
        return Lists.transform(entities, this::transform);
    }

}
