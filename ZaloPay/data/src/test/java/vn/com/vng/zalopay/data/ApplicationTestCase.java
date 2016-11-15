package vn.com.vng.zalopay.data;

import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Created by hieuvm on 11/14/16.
 */

@RunWith(CustomRobolectricRunner.class)
@Config(constants = BuildConfig.class, sdk = 16, application = AndroidApplicationTest.class)
public abstract class ApplicationTestCase {
}