package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.BankCard;

/**
 * Created by AnhHieu on 5/18/16.
 */

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
        if (Lists.isEmptyOrNull(cards)) return Collections.emptyList();
        List<BankCard> list = new ArrayList<>();
        for (CardEntity dMappedCard : cards) {
            BankCard bCard = transform(dMappedCard);
            if (bCard != null) {
                list.add(bCard);
            }
        }
        return list;
    }

}
