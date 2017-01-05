package vn.com.vng.zalopay.data;

/**
 * Created by AnhHieu on 5/5/16.
 */
public class Constants {
    public static final int NUMBER_RETRY_REST = 3;

    public static final String MANIF_BALANCE = "manif_balance";

    public static final String MANIF_PLATFORM_INFO_CHECKSUM = "manif_pfinfochecksum";
    public static final String MANIF_RESOURCE_VERSION = "manif_resource_version";

    public static final String MANIFEST_RESOURCE_INTERNAL_VERSION = "manifest_resource_internal_version";
    public static final String MANIFEST_RESOURCE_EXTERNAL_VERSION = "manifest_resource_internal_version";

    public static final String ZPTRANSTOKEN = "zptranstoken";
    public static final String APPID = "appid";

    public static final String AMOUNT = "amount";
    public static final String TRANSTYPE = "transtype";
    public static final String APPUSER = "appuser";
    public static final String DESCRIPTION = "description";
    public static final String EMBEDDATA = "embeddata";

    public static final String MANIF_LASTTIME_UPDATE_ZALO_FRIEND = "manif_lt_zalo_friend";
    public static final String MANIFEST_LOADED_TRANSACTION_SUCCESS = "manifest_loaded_transaction_success";
    public static final String MANIFEST_LOADED_TRANSACTION_FAIL = "manifest_loaded_transaction_fail";
    public static final String MANIFEST_RECOVERY_TIME_NOTIFICATION = "manifest_recovery_notification";
    public static final String MANIFEST_LASTTIME_SYNC_CONTACT = "manifest_lt_sync_contact";
    public static final String MANIFEST_RECOVERY_NOTIFY = "manifest_notify_recovery";

    public interface TPE_API {
        String CREATEWALLETORDER = "v001/tpe/createwalletorder";
        String GETBALANCE = "v001/tpe/getbalance";
        String GETINSIDEAPPRESOURCE = "v001/tpe/getinsideappresource";
        String GETORDERINFO = "v001/tpe/getorderinfo";
        String GETTRANSSTATUS = "v001/tpe/gettransstatus";
        String TRANSHISTORY = "v001/tpe/transhistory";
    }

    public interface UM_API {
        String CREATEACCESSTOKEN = "um/createaccesstoken";
        String REMOVEACCESSTOKEN = "um/removeaccesstoken";
        String VERIFYCODETEST = "/um/verifycodetest";
        String UPDATEPROFILE = "um/updateprofile";
        String VERIFYOTPPROFILE = "um/verifyotpprofile";
        String RECOVERYPIN = "um/recoverypin";
        String GETUSERINFO = "um/getuserinfo";
        String GETUSERPROFILELEVEL = "um/getuserprofilelevel";
        String GETUSERINFOBYZALOPAYNAME = "um/getuserinfobyzalopayname";
        String GETUSERINFOBYZALOPAYID = "um/getuserinfobyzalopayid";
        String CHECKZALOPAYNAMEEXIST = "um/checkzalopaynameexist";
        String UPDATEZALOPAYNAME = "um/updatezalopayname";
        String VALIDATEPIN = "um/validatepin";
        String SENDNOTIFICATION = "um/sendnotification";
        String GETMERCHANTUSERINFO = "ummerchant/getmerchantuserinfo";
        String GETLISTMERCHANTUSERINFO = "ummerchant/getlistmerchantuserinfo";
    }

    public interface UMUPLOAD_API {
        String PREUPDATEPROFILELEVEL3 = "umupload/preupdateprofilelevel3";
    }

    public interface REDPACKET_API {
        String CREATEBUNDLEORDER = "redpackage/createbundleorder";
        String SUBMITTOSENDBUNDLE = "redpackage/submittosendbundle";
        String SUBMITOPENPACKAGE = "redpackage/submitopenpackage";
        String GETSENTBUNDLELIST = "redpackage/getsentbundlelist";
        String GETREVPACKAGELIST = "redpackage/getrevpackagelist";
        String GETPACKAGESINBUNDLE = "redpackage/getpackagesinbundle";
        String GETAPPINFO = "redpackage/getappinfo";
        String SUBMITTOSENDBUNDLEBYZALOPAYINFO = "redpackage/submittosendbundlebyzalopayinfo";
        String GET_LIST_PACKAGE_STATUS = "redpackage/getlistpackagestatus";
    }
}
