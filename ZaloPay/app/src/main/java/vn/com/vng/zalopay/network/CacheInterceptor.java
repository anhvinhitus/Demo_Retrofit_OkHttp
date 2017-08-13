package vn.com.vng.zalopay.network;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by hieuvm on 8/13/17.
 * *
 */

public class CacheInterceptor implements Interceptor {

    private final Context mContext;
    private static final String CACHE_CONTROL = "Cache-Control";
    public CacheInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed( chain.request() );

        // re-write response header to force use of cache
        CacheControl cacheControl = new CacheControl.Builder()
                .maxAge( 5, TimeUnit.MINUTES )
                .build();

        return response.newBuilder()
                .header( CACHE_CONTROL, cacheControl.toString() )
                .build();
    }
}
