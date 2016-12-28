package vn.com.vng.zalopay.react.model;

import java.util.List;

import vn.com.vng.zalopay.domain.model.TransHistory;

/**
 * Created by hieuvm on 12/27/16.
 */

public class TransactionResult extends AbstractReactResult<List<TransHistory>> {

    public TransactionResult(int code, String message, List<TransHistory> data) {
        super(code, message, data);
    }
}
