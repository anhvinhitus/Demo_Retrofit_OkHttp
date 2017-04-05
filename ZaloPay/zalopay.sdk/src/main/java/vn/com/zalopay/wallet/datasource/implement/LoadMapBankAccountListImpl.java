package vn.com.zalopay.wallet.datasource.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.base.BankAccountListResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class LoadMapBankAccountListImpl implements IRequest<BankAccountListResponse> {
    @Override
    public Observable<BankAccountListResponse> getRequest(IData pIData, Map<String, String> pParams) {
        return pIData.loadMapBankAccountList(pParams);
    }
}
