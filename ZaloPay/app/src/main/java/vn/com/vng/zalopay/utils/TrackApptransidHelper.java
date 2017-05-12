package vn.com.vng.zalopay.utils;

import vn.com.vng.zalopay.Constants;
import vn.com.zalopay.analytics.ZPPaymentSteps;

/**
 * Created by khattn on 4/28/17.
 * Class help transform constants source from transfer to track apptransid
 */

public class TrackApptransidHelper {
    public static String transfrom(Constants.ActivateSource activateSource) {
        switch (activateSource) {
            case FromTransferActivity:
                return null;
            case FromQRCodeType1:
                return ZPPaymentSteps.OrderSource_QR;
            case FromQRCodeType2:
                return ZPPaymentSteps.OrderSource_QR;
            case FromZalo:
                return ZPPaymentSteps.OrderSource_AppToApp;
            case FromWebApp_QRType2:
                return ZPPaymentSteps.OrderSource_WebToApp;
        }

        return null;
    }
}
