package vn.com.vng.zalopay.data.api.entity.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import vn.com.vng.zalopay.data.api.entity.TransHistoryEntity;
import vn.com.vng.zalopay.data.api.response.GetOrderResponse;
import vn.com.vng.zalopay.data.util.Lists;
import vn.com.vng.zalopay.domain.model.Order;
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
        if (val == null) {
            return null;
        }

        TransHistory item = new TransHistory(val.transid);
        item.appid = val.appid;
        item.appuser = val.appuser;
        item.description = val.description;
        item.userchargeamt = val.userchargeamt;
        item.userfeeamt = val.userfeeamt;
        item.amount = val.amount;
        item.platform = val.platform;
        item.pmcid = val.pmcid;
        item.type = val.type;
        item.reqdate = val.reqdate;
        item.userid = val.userid;
        item.sign = val.sign;
        item.username = val.username;
        item.appusername = val.appusername;

        return item;
    }

    public Order transform(GetOrderResponse getOrderResponse) {
        return new Order(getOrderResponse.getAppid(),
                getOrderResponse.getZptranstoken(),
                getOrderResponse.apptransid,
                getOrderResponse.appuser,
                getOrderResponse.apptime,
                getOrderResponse.embeddata,
                getOrderResponse.item,
                getOrderResponse.amount,
                getOrderResponse.description,
                getOrderResponse.payoption,
                getOrderResponse.mac);
    }
}
