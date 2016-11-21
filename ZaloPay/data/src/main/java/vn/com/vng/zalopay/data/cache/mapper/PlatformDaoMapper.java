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


    public AppResourceGD transform(AppResourceEntity appResourceEntity) {
        AppResourceGD appResourceGD = null;
        if (appResourceEntity != null) {
            appResourceGD = new AppResourceGD();
            appResourceGD.setAppid(appResourceEntity.appid);
            appResourceGD.setAppname(appResourceEntity.appname);
            appResourceGD.setChecksum(appResourceEntity.checksum);
            appResourceGD.setImageurl(appResourceEntity.imageurl);
            appResourceGD.setJsurl(appResourceEntity.jsurl);
            appResourceGD.setNeeddownloadrs(appResourceEntity.needdownloadrs);
            appResourceGD.setStatus(appResourceEntity.status);
            appResourceGD.setApptype(appResourceEntity.apptype);
            appResourceGD.setWeburl(appResourceEntity.weburl);
            appResourceGD.setIconurl(appResourceEntity.iconurl);
            appResourceGD.setSortOrder(appResourceEntity.sortOrder);
            appResourceGD.setStateDownload(appResourceEntity.stateDownload);
            appResourceGD.setNumRetry(appResourceEntity.numRetry);
            appResourceGD.setTimeDownload(appResourceEntity.timeDownload);
        }
        return appResourceGD;
    }

    public AppResourceEntity transform(AppResourceGD appResourceGD) {
        AppResourceEntity appResourceEntity = null;
        if (appResourceGD != null) {
            appResourceEntity = new AppResourceEntity();
            appResourceEntity.appid = appResourceGD.getAppid();
            appResourceEntity.appname = appResourceGD.getAppname();
            appResourceEntity.checksum = appResourceGD.getChecksum();
            appResourceEntity.imageurl = appResourceGD.getImageurl();
            appResourceEntity.needdownloadrs = appResourceGD.getNeeddownloadrs() == null ? 0 : 1;
            appResourceEntity.status = appResourceGD.getStatus() == null ? 0 : 1;
            appResourceEntity.jsurl = appResourceGD.getJsurl() == null
                    ? "" : appResourceGD.getJsurl();
            appResourceEntity.apptype = appResourceGD.getApptype() == null
                    ? 0 : appResourceGD.getApptype();
            appResourceEntity.weburl = appResourceGD.getWeburl() == null
                    ? "" : appResourceGD.getWeburl();
            appResourceEntity.iconurl = appResourceGD.getIconurl() == null
                    ? "" : appResourceGD.getIconurl();
            appResourceEntity.sortOrder = appResourceGD.getSortOrder() == null
                    ? 0 : appResourceGD.getSortOrder();
            appResourceEntity.stateDownload = appResourceGD.getStateDownload() == null
                    ? 0 : appResourceGD.getStateDownload();
            appResourceEntity.numRetry = appResourceGD.getNumRetry() == null
                    ? 0 : appResourceGD.getNumRetry();
            appResourceEntity.timeDownload = appResourceGD.getTimeDownload() == null
                    ? 0 : appResourceGD.getTimeDownload();

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
                PaymentTransTypeGD paymentTransTypeGD = new PaymentTransTypeGD(appResourceEntity.transtype);
                paymentTransTypeGD.setPmcid(pcmEntity.pmcid);
                paymentTransTypeGD.setStatus(pcmEntity.status);
                paymentTransTypeGD.setFeecaltype(pcmEntity.feecaltype);
                paymentTransTypeGD.setFeerate(pcmEntity.feerate);
                paymentTransTypeGD.setMinvalue(pcmEntity.minvalue);
                paymentTransTypeGD.setMaxvalue(pcmEntity.maxvalue);
                paymentTransTypeGD.setPmcname(pcmEntity.pmcname);
                paymentTransTypeGD.setMinfee(pcmEntity.minxfee);

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
