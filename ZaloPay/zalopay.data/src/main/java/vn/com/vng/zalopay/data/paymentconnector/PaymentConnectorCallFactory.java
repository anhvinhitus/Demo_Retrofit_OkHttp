package vn.com.vng.zalopay.data.paymentconnector;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import vn.com.vng.zalopay.data.util.ConfigUtil;
import vn.com.vng.zalopay.data.util.Strings;

/**
 * Created by hieuvm on 3/9/17.
 * Call.Factory for creating PaymentConnectorCall
 */

public class PaymentConnectorCallFactory implements Call.Factory {

    private final PaymentConnectorService mConnectorService;
    private final OkHttpClient mOkHttpClient;

    public PaymentConnectorCallFactory(PaymentConnectorService connectorService, OkHttpClient okHttpClient) {
        this.mConnectorService = connectorService;
        this.mOkHttpClient = okHttpClient;
    }

    @Override
    public Call newCall(Request request) {
        if (ConfigUtil.isHttpsRoute()) {
            return mOkHttpClient.newCall(request);
        }

        if (!mConnectorService.isConnectionReady()) {
            return mOkHttpClient.newCall(request);
        }

        String apiName = Strings.joinWithDelimiter("/", request.url().pathSegments());
        if (ConfigUtil.containsApi(apiName)) {
            return new PaymentConnectorCall(mConnectorService, request);
        } else {
            return mOkHttpClient.newCall(request);
        }

    }
}
