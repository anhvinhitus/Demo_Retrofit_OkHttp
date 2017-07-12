package vn.com.vng.zalopay;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;


/**
 * Created by datnt10 on 7/12/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = vn.com.vng.zalopay.BuildConfig.class, sdk = 21, application = AndroidApplicationTest.class, manifest = Config.NONE)
public abstract class ApplicationTestCase {
}