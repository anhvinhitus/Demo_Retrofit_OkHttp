package vn.com.vng.zalopay.data.cache.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.cache.model.BankCardGD;
import vn.com.vng.zalopay.data.util.Lists;

import static java.util.Collections.emptyList;

/**
 * Created by AnhHieu on 5/18/16.
 */

@Singleton
public class PlatformDaoMapper {

    @Inject
    public PlatformDaoMapper() {
    }

    public BankCardGD transform(CardEntity cardEntity) {
        BankCardGD bankCardGD = null;
        if (cardEntity != null) {
            bankCardGD = new BankCardGD(cardEntity.cardhash);
            bankCardGD.setBankcode(cardEntity.bankcode);
            bankCardGD.setCardname(cardEntity.cardname);
            bankCardGD.setFirst6cardno(cardEntity.first6cardno);
            bankCardGD.setLast4cardno(cardEntity.last4cardno);
        }
        return bankCardGD;
    }

    public CardEntity transform(BankCardGD cardGD) {
        CardEntity cardEntity = null;
        if (cardGD != null) {
            cardEntity = new CardEntity();
            cardEntity.bankcode = cardGD.getBankcode();
            cardEntity.cardhash = cardGD.getCardhash();
            cardEntity.cardname = cardGD.getCardname();
            cardEntity.first6cardno = cardGD.getFirst6cardno();
            cardEntity.last4cardno = cardGD.getLast4cardno();

        }
        return cardEntity;
    }

    public List<CardEntity> transformCardEntity(Collection<BankCardGD> cardGDs) {
        if (Lists.isEmptyOrNull(cardGDs))
            return emptyList();

        List<CardEntity> cardEntities = new ArrayList<>(cardGDs.size());
        for (BankCardGD cardGD : cardGDs) {
            CardEntity cardEntity = transform(cardGD);
            if (cardEntity != null) {
                cardEntities.add(cardEntity);
            }
        }
        return cardEntities;
    }

    public List<BankCardGD> transformCardGreenDao(Collection<CardEntity> cardEntities) {
        if (Lists.isEmptyOrNull(cardEntities))
            return emptyList();

        List<BankCardGD> bankCardGDs = new ArrayList<>(cardEntities.size());
        for (CardEntity cardEntity : cardEntities) {
            BankCardGD bankCardGD = transform(cardEntity);
            if (bankCardGD != null) {
                bankCardGDs.add(bankCardGD);
            }
        }
        return bankCardGDs;
    }
}
