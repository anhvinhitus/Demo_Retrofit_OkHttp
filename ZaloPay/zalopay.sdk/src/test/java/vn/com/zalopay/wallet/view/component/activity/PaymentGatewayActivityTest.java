package vn.com.zalopay.wallet.view.component.activity;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huuhoa on 5/31/17.
 */
public class PaymentGatewayActivityTest {
    @Test
    public void onBackPressed() throws Exception {

    }

    @Test
    public void onSelectedChannel() {
        PaymentGatewayActivity activity = new PaymentGatewayActivity();
        activity.onSelectedChannel(null);
    }
}
