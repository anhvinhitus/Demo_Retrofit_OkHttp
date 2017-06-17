package vn.com.zalopay.wallet.api;

import android.os.Build;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

import vn.com.zalopay.wallet.business.entity.base.BaseResponse;

/**
 * Created by chucvv on 6/17/17.
 */

public abstract class AbstractRequest<T extends BaseResponse> implements IRequest<T> {
    protected ITransService mTransService;

    public AbstractRequest(ITransService transService) {
        mTransService = transService;
    }

    @Override
    public Map<String, String> buildParams() {
        return null;
    }

    protected Map<String, String> getMapTable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new ArrayMap<>();
        } else {
            return new HashMap<>();
        }
    }
}
