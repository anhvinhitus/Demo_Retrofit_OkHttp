package vn.com.zalopay.wallet.datasource.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.CardInfoListResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class LoadMapCardListImpl implements IRequest<CardInfoListResponse> {
    @Override
    public Observable<CardInfoListResponse> getRequest(IData pIData, Map<String, String> pParams) {
        return pIData.loadMapCardList(pParams);
    }
}
