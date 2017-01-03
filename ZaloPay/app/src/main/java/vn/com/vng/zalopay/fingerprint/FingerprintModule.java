package vn.com.vng.zalopay.fingerprint;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

/**
 * Created by hieuvm on 12/26/16.
 */
@Module
public class FingerprintModule {

    @Provides
    public FingerprintProvider providersFingerprintProvider(Context context) {
        return new FingerprintProvider(context);
    }

    @Provides
    public KeyTools providesKeystore(Context context) {
        return new KeyTools(context);
    }

}
