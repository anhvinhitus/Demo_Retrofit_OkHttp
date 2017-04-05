package vn.com.zalopay.wallet.datasource.implement;

import java.util.Map;

import rx.Observable;
import vn.com.zalopay.wallet.business.entity.atm.BankConfigResponse;
import vn.com.zalopay.wallet.datasource.IData;
import vn.com.zalopay.wallet.datasource.interfaces.IRequest;

public class LoadBankListImpl implements IRequest<BankConfigResponse> {

    @Override
    public Observable<BankConfigResponse> getRequest(IData pIData, Map<String, String> pParams) {
        return pIData.loadBankList(pParams);
    }
}
