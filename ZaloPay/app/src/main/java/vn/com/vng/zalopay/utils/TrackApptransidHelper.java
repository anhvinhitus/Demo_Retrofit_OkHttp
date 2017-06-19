package vn.com.vng.zalopay.utils;

import vn.com.vng.zalopay.Constants;
import vn.com.zalopay.analytics.ZPPaymentSteps;

/**
 * Created by khattn on 4/28/17.
 * Class help transform constants source from transfer to track apptransid
 */

public class TrackApptransidHelper {
    public static int transform(Constants.ActivateSource activateSource) {
        switch (activateSource) {
            case FromTransferActivity:
                return 0;
            case FromQRCodeType1:
                return ZPPaymentSteps.OrderSource_QR;
            case FromQRCodeType2:
                return ZPPaymentSteps.OrderSource_QR;
            case FromZalo:
                return ZPPaymentSteps.OrderSource_Zalo;
            case FromWebApp_QRType2:
                return ZPPaymentSteps.OrderSource_WebToApp;
        }

        return 0;
    }
}
