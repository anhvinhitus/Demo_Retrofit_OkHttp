package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.TransHistory;

import static java.util.Collections.emptyList;

/**
 * Created by AnhHieu on 5/9/16.
 */

@Singleton
public class ZaloPayEntityDataMapper {

    @Inject
    public ZaloPayEntityDataMapper() {
    }

    public TransHistory transform(TransHistoryEntity val) {
        TransHistory transHistory = null;
        if (val != null) {
            transHistory = new TransHistory(val.transid);
            transHistory.appid = val.appid;
            transHistory.appuser = val.appuser;
            transHistory.description = val.description;
            transHistory.grossamount = val.grossamount;
            transHistory.netamount = val.netamount;
            transHistory.platform = val.platform;
            transHistory.pmcid = val.pmcid;
            transHistory.type = val.type;
            transHistory.reqdate = val.reqdate;
            transHistory.userid = val.userid;
        }

        return transHistory;
    }

    public List<TransHistory> transform(Collection<TransHistoryEntity> transHistoryEntities) {
        if (Lists.isEmptyOrNull(transHistoryEntities))
            return emptyList();

        List<TransHistory> transHistories = new ArrayList<>(transHistoryEntities.size());
        for (TransHistoryEntity transHistoryEntity : transHistoryEntities) {
            TransHistory transHistory = transform(transHistoryEntity);
            if (transHistory != null) {
                transHistories.add(transHistory);
            }
        }
        return transHistories;
    }
}
