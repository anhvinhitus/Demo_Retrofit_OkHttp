package vn.com.vng.zalopay.data.cache.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.AppResourceEntity;
import vn.com.vng.zalopay.data.api.entity.CardEntity;
import vn.com.vng.zalopay.data.api.entity.PCMEntity;
import vn.com.vng.zalopay.data.api.entity.PaymentTransTypeEntity;
import vn.com.vng.zalopay.data.cache.model.AppResourceGD;
import vn.com.vng.zalopay.data.cache.model.BankCardGD;
import vn.com.vng.zalopay.data.cache.model.PaymentTransTypeGD;
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
            bankCardGD = new BankCardGD();
            bankCardGD.cardhash = cardEntity.cardhash;
            bankCardGD.bankcode = (cardEntity.bankcode);
            bankCardGD.cardname = (cardEntity.cardname);
            bankCardGD.first6cardno = (cardEntity.first6cardno);
            bankCardGD.last4cardno = (cardEntity.last4cardno);
        }
        return bankCardGD;
    }

    public CardEntity transform(BankCardGD cardGD) {
        CardEntity cardEntity = null;
        if (cardGD != null) {
            cardEntity = new CardEntity();
            cardEntity.bankcode = cardGD.bankcode;
            cardEntity.cardhash = cardGD.cardhash;
            cardEntity.cardname = cardGD.cardname;
            cardEntity.first6cardno = cardGD.first6cardno;
            cardEntity.last4cardno = cardGD.last4cardno;

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


    public AppResourceGD transform(AppResourceEntity appResourceEntity) {
        AppResourceGD appResourceGD = null;
        if (appResourceEntity != null) {
            appResourceGD = new AppResourceGD();
            appResourceGD.appid = (appResourceEntity.appid);
            appResourceGD.appname = (appResourceEntity.appname);
            appResourceGD.checksum = (appResourceEntity.checksum);
            appResourceGD.imageurl = (appResourceEntity.imageurl);
            appResourceGD.jsurl = (appResourceEntity.jsurl);
            appResourceGD.needdownloadrs = (appResourceEntity.needdownloadrs);
            appResourceGD.status = (appResourceEntity.status);
            appResourceGD.apptype = (appResourceEntity.apptype);
            appResourceGD.weburl = (appResourceEntity.weburl);
            appResourceGD.iconurl = (appResourceEntity.iconurl);
            appResourceGD.sortOrder = (appResourceEntity.sortOrder);
            appResourceGD.stateDownload = (appResourceEntity.stateDownload);
            appResourceGD.numRetry = (appResourceEntity.numRetry);
            appResourceGD.timeDownload = (appResourceEntity.timeDownload);
        }
        return appResourceGD;
    }

    public AppResourceEntity transform(AppResourceGD appResourceGD) {
        AppResourceEntity appResourceEntity = null;
        if (appResourceGD != null) {
            appResourceEntity = new AppResourceEntity();
            appResourceEntity.appid = appResourceGD.appid;
            appResourceEntity.appname = appResourceGD.appname;
            appResourceEntity.checksum = appResourceGD.checksum;
            appResourceEntity.imageurl = appResourceGD.imageurl;
            appResourceEntity.needdownloadrs = appResourceGD.needdownloadrs == null ? 0 : 1;
            appResourceEntity.status = appResourceGD.status == null ? 0 : 1;
            appResourceEntity.jsurl = appResourceGD.jsurl == null
                    ? "" : appResourceGD.jsurl;
            appResourceEntity.apptype = appResourceGD.apptype == null
                    ? 0 : appResourceGD.apptype;
            appResourceEntity.weburl = appResourceGD.weburl == null
                    ? "" : appResourceGD.weburl;
            appResourceEntity.iconurl = appResourceGD.iconurl == null
                    ? "" : appResourceGD.iconurl;
            appResourceEntity.sortOrder = appResourceGD.sortOrder == null
                    ? 0 : appResourceGD.sortOrder;
            appResourceEntity.stateDownload = appResourceGD.stateDownload == null
                    ? 0 : appResourceGD.stateDownload;
            appResourceEntity.numRetry = appResourceGD.numRetry == null
                    ? 0 : appResourceGD.numRetry;
            appResourceEntity.timeDownload = appResourceGD.timeDownload == null
                    ? 0 : appResourceGD.timeDownload;

        }
        return appResourceEntity;
    }

    public List<AppResourceGD> transformAppResourceEntity(Collection<AppResourceEntity> appResourceEntities) {
        if (Lists.isEmptyOrNull(appResourceEntities))
            return emptyList();

        List<AppResourceGD> appResourceGDs = new ArrayList<>(appResourceEntities.size());
        for (AppResourceEntity appResourceEntity : appResourceEntities) {
            AppResourceGD appResourceGD = transform(appResourceEntity);
            if (appResourceGD != null) {
                appResourceGDs.add(appResourceGD);
            }
        }
        return appResourceGDs;
    }

    public List<AppResourceEntity> transformAppResourceDao(Collection<AppResourceGD> appResourceGDs) {
        if (Lists.isEmptyOrNull(appResourceGDs))
            return emptyList();

        List<AppResourceEntity> appResourceEntities = new ArrayList<>(appResourceGDs.size());
        for (AppResourceGD appResourceGD : appResourceGDs) {
            AppResourceEntity appResourceEntity = transform(appResourceGD);
            if (appResourceEntity != null) {
                appResourceEntities.add(appResourceEntity);
            }
        }
        return appResourceEntities;
    }


    public List<PaymentTransTypeGD> transform(PaymentTransTypeEntity appResourceEntity) {
        List<PaymentTransTypeGD> listPaymentTransDao = null;
        if (appResourceEntity != null && !Lists.isEmptyOrNull(appResourceEntity.pmclist)) {
            listPaymentTransDao = new ArrayList<>();

            for (PCMEntity pcmEntity : appResourceEntity.pmclist) {
                PaymentTransTypeGD paymentTransTypeGD = new PaymentTransTypeGD();
                paymentTransTypeGD.transtype = appResourceEntity.transtype;
                paymentTransTypeGD.pmcid = (pcmEntity.pmcid);
                paymentTransTypeGD.status = (pcmEntity.status);
                paymentTransTypeGD.feecaltype = (pcmEntity.feecaltype);
                paymentTransTypeGD.feerate = (pcmEntity.feerate);
                paymentTransTypeGD.minvalue = (pcmEntity.minvalue);
                paymentTransTypeGD.maxvalue = (pcmEntity.maxvalue);
                paymentTransTypeGD.pmcname = (pcmEntity.pmcname);
                paymentTransTypeGD.minfee = (pcmEntity.minxfee);

                listPaymentTransDao.add(paymentTransTypeGD);
            }
        }
        return listPaymentTransDao;
    }


    public List<PaymentTransTypeGD> transformPaymentTransTypeEntity(List<PaymentTransTypeEntity> appResourceEntity) {
        if (Lists.isEmptyOrNull(appResourceEntity))
            return emptyList();
        List<PaymentTransTypeGD> appResourceEntities = new ArrayList<>();
        for (PaymentTransTypeEntity paymentTransTypeEntity : appResourceEntity) {
            if (paymentTransTypeEntity == null) continue;
            appResourceEntities.addAll(transform(paymentTransTypeEntity));
        }
        return appResourceEntities;

    }
}
