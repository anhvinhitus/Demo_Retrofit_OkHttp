package vn.com.vng.zalopay.data;

/**
 * Created by AnhHieu on 5/5/16.
 */
public class Constants {
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
    public static final String MANIFEST_RECOVERY_NOTIFICATION = "manifest_recovery_notification";

    public interface API {
        String CREATEWALLETORDER = "v001/tpe/createwalletorder";
        String GETBALANCE = "v001/tpe/getbalance";
        String GETINSIDEAPPRESOURCE = "v001/tpe/getinsideappresource";
        String GETORDERINFO = "v001/tpe/getorderinfo";
        String GETTRANSSTATUS = "v001/tpe/gettransstatus";
        String INSIDEAPPRESOURCE = "v001/tpe/insideappresource";
        String TRANSHISTORY = "v001/tpe/transhistory";
    }
}
