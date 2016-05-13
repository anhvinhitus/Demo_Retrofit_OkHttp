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
            transDao.setGrossamount(transEntity.grossamount);
            transDao.setPlatform(transEntity.platform);
            transDao.setPmcid(transEntity.pmcid);
            transDao.setType(transEntity.type);
            transDao.setReqdate(transEntity.reqdate);
            transDao.setNetamount(transEntity.netamount);
            transDao.setUserid(transEntity.userid);
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
            transHistoryEntity.grossamount = transDao.getGrossamount();
            transHistoryEntity.netamount = transDao.getNetamount();
            transHistoryEntity.platform = transDao.getPlatform();
            transHistoryEntity.pmcid = transDao.getPmcid();
            transHistoryEntity.reqdate = transDao.getReqdate();
            transHistoryEntity.transid = transDao.getTransid();
            transHistoryEntity.type = transDao.getType();
            transHistoryEntity.userid = transDao.getUserid();

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
