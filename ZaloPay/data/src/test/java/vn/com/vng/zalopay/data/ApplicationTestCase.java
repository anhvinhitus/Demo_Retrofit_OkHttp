package vn.com.vng.zalopay.data;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by hieuvm on 11/14/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 16, application = AndroidApplicationTest.class, manifest = "src/main/AndroidManifest.xml", packageName = "vn.com.vng.zalopay.data")
public abstract class ApplicationTestCase {
}