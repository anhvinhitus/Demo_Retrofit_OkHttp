package vn.com.vng.zalopay.data.paymentconnector;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import timber.log.Timber;
import vn.com.vng.zalopay.data.util.ConfigLoader;
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
        if (ConfigLoader.isHttpsRoute()) {
            return mOkHttpClient.newCall(request);
        }

        if (!mConnectorService.isConnectionReady()) {
            Timber.d("[NOTCONNECT] Reroute to https for request: %s", request.url().encodedPath());
            return mOkHttpClient.newCall(request);
        }

        String apiName = Strings.joinWithDelimiter("/", request.url().pathSegments());
        if (ConfigLoader.containsApi(apiName)) {
            return new PaymentConnectorCall(mConnectorService, request);
        } else {
            Timber.d("[SKIP] Reroute to https for request: %s", request.url().encodedPath());
            return mOkHttpClient.newCall(request);
        }

    }
}
