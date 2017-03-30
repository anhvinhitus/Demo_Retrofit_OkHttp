package vn.com.zalopay.wallet.datasource.implement;

import java.util.HashMap;

import retrofit2.Response;
import rx.Observable;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class LoadMapCardListImpl implements IRequest<CardInfoListResponse> {
    @Override
    public Observable<Response<CardInfoListResponse>> getRequest(IData pIData, HashMap<String, String> pParams) {
        return pIData.loadMapCardList(pParams);
    }

    @Override
    public int getRequestEventId() {
        return ZPEvents.CONNECTOR_UM_LISTCARDINFOFORCLIENT;
    }
}
