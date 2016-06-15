package vn.com.vng.zalopay.data.cache.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.cache.model.TransactionLog;
import vn.com.vng.zalopay.data.util.Lists;

import static java.util.Collections.emptyList;

/**
 * Created by AnhHieu on 5/9/16.
 * Map data object from server to local storage and vice versa
 */
@Singleton
public class ZaloPayDaoMapper {

    @Inject
    public ZaloPayDaoMapper() {
    }

    public TransactionLog transform(TransHistoryEntity transEntity) {
        TransactionLog transDao = null;
        if (transEntity != null) {
            transDao = new TransactionLog(transEntity.transid);
            transDao.setAppuser(transEntity.appuser);
            transDao.setAppid(transEntity.appid);
            transDao.setDescription(transEntity.description);
            transDao.setUserchargeamt(transEntity.userchargeamt);
            transDao.setUserfeeamt(transEntity.userfeeamt);
            transDao.setAmount(transEntity.amount);
            transDao.setPlatform(transEntity.platform);
            transDao.setPmcid(transEntity.pmcid);
            transDao.setType(transEntity.type);
            transDao.setReqdate(transEntity.reqdate);
            transDao.setUserid(transEntity.userid);
            transDao.setSign(transEntity.sign);
            transDao.setUsername(transEntity.username);
            transDao.setAppusername(transEntity.appusername);
        }
        return transDao;
    }

    public TransHistoryEntity transform(TransactionLog transDao) {
        TransHistoryEntity transHistoryEntity = null;
        if (transDao != null) {
            transHistoryEntity = new TransHistoryEntity();
            transHistoryEntity.appid = transDao.getAppid();
            transHistoryEntity.appuser = transDao.getAppuser();
            transHistoryEntity.description = transDao.getDescription();
            transHistoryEntity.userchargeamt = transDao.getUserchargeamt();
            transHistoryEntity.userfeeamt = transDao.getUserfeeamt();
            transHistoryEntity.amount = transDao.getAmount();
            transHistoryEntity.platform = transDao.getPlatform();
            transHistoryEntity.pmcid = transDao.getPmcid();
            transHistoryEntity.reqdate = transDao.getReqdate();
            transHistoryEntity.transid = transDao.getTransid();
            transHistoryEntity.type = transDao.getType();
            transHistoryEntity.userid = transDao.getUserid();
            transHistoryEntity.sign = transDao.getSign();
            transHistoryEntity.username = transDao.getUsername();
            transHistoryEntity.appusername = transDao.getAppusername();
        }
        return transHistoryEntity;
    }

    public List<TransactionLog> transform(Collection<TransHistoryEntity> transHistoryEntities) {
        if (Lists.isEmptyOrNull(transHistoryEntities))
            return emptyList();

        List<TransactionLog> transactionLogs = new ArrayList<>(transHistoryEntities.size());
        for (TransHistoryEntity transHistoryEntity : transHistoryEntities) {
            TransactionLog transactionLog = transform(transHistoryEntity);
            if (transactionLog != null) {
                transactionLogs.add(transactionLog);
            }
        }
        return transactionLogs;
    }

    public List<TransHistoryEntity> transform2Entity(Collection<TransactionLog> transactionLogs) {
        if (Lists.isEmptyOrNull(transactionLogs))
            return emptyList();

        List<TransHistoryEntity> transHistoryEntities = new ArrayList<>(transactionLogs.size());
        for (TransactionLog transactionLog : transactionLogs) {
            TransHistoryEntity transHistoryEntity = transform(transactionLog);
            if (transHistoryEntity != null) {
                transHistoryEntities.add(transHistoryEntity);
            }
        }
        return transHistoryEntities;
    }
}
