package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.Collections;
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
            bankCard = new BankCard(card.cardname, card.first6cardno, card.last4cardno, card.bankcode, 0);
        }
        return bankCard;
    }

    public List<BankCard> transform(List<CardEntity> cards) {
        return Lists.transform(cards, this::transform);
//        if (Lists.isEmptyOrNull(cards)) return Collections.emptyList();
//        List<BankCard> list = new ArrayList<>();
//        for (CardEntity dMappedCard : cards) {
//            BankCard bCard = transform(dMappedCard);
//            if (bCard != null) {
//                list.add(bCard);
//            }
//        }
//
//        return list;
    }


    public AppResource transform(AppResourceEntity appResourceEntity) {
        AppResource appResource = null;
        if (appResourceEntity != null) {
            appResource = new AppResource();
            appResource.checksum = appResourceEntity.checksum;
            appResource.appname = appResourceEntity.appname;
            appResource.status = appResourceEntity.status;
            appResource.appid = appResourceEntity.appid;
            appResource.appType = appResourceEntity.apptype;
            appResource.webUrl = appResourceEntity.weburl;
            appResource.urlImage = appResourceEntity.imageurl;
            appResource.iconUrl = appResourceEntity.iconurl;
        }
        return appResource;
    }

    public List<AppResource> transformAppResourceEntity(List<AppResourceEntity> appResourceEntities) {
        return Lists.transform(appResourceEntities, this::transform);
//        if (Lists.isEmptyOrNull(appResourceEntities)) return Collections.emptyList();
//        List<AppResource> list = new ArrayList<>();
//        for (AppResourceEntity appResourceEntity : appResourceEntities) {
//            AppResource appResource = transform(appResourceEntity);
//            if (appResource != null) {
//                list.add(appResource);
//            }
//        }
//        return list;
    }

}
