package vn.com.vng.zalopay.analytics;

/**
 * Auto-generated
 */
public class ZPEvents {
    public static final int APP_LAUNCH = 1000;
    public static final int TAP_LOGIN = 1100;
    public static final int LOGINFAILED_NONETWORK = 1101;
    public static final int LOGINFAILED_USERDENIED = 1102;
    public static final int LOGINSUCCESS_ZALO = 1110;
    public static final int LOGINFAILED_API_ERROR = 1111;
    public static final int NEEDINVITATIONCODE = 1112;
    public static final int APPLAUNCHHOMEFROMLOGIN = 1113;
    public static final int INVITATIONFROMLOGIN = 1114;
    public static final int INPUTINVITATIONCODE = 1200;
    public static final int INVITATIONCODEWRONG = 1201;
    public static final int INVITATIONCODEREUSED = 1202;
    public static final int INVITATIONCODESUCCESS = 1203;
    public static final int APPLAUNCHHOMEFROMINVITATION = 1204;
    public static final int APPLAUNCHHOME = 1300;
    public static final int TAPSCANQR = 1301;
    public static final int TAPADDCASH = 1302;
    public static final int TAPMANAGECARDS = 1303;
    public static final int TAPMENUBUTTON = 1304;
    public static final int TAPNOTIFICATIONBUTTON = 1305;
    public static final int TAPBANNERPOSITION1 = 1306;
    public static final int TAPBANNERPOSITION2 = 1307;
    public static final int TAPBANNERPOSITION3 = 1308;
    public static final int TAPAPPICON_1_1 = 1309;
    public static final int TAPAPPICON_1_2 = 1310;
    public static final int TAPAPPICON_1_3 = 1311;
    public static final int TAPAPPICON_1_4 = 1312;
    public static final int TAPAPPICON_2_1 = 1313;
    public static final int TAPAPPICON_2_2 = 1314;
    public static final int TAPAPPICON_2_3 = 1315;
    public static final int TAPAPPICON_2_4 = 1316;
    public static final int TAPAPPICON_3_1 = 1317;
    public static final int TAPAPPICON_3_2 = 1318;
    public static final int TAPAPPICON_3_3 = 1319;
    public static final int TAPAPPICON_3_4 = 1320;
    public static final int TAPAPPICON_4_1 = 1321;
    public static final int TAPAPPICON_4_2 = 1322;
    public static final int TAPAPPICON_4_3 = 1323;
    public static final int TAPAPPICON_4_4 = 1324;
    public static final int TAPAPPICON_5_1 = 1325;
    public static final int TAPAPPICON_5_2 = 1326;
    public static final int TAPAPPICON_5_3 = 1327;
    public static final int TAPAPPICON_5_4 = 1328;
    public static final int OPENLEFTMENU = 1400;
    public static final int TAPLEFTMENUUSERPROFILE = 1401;
    public static final int TAPLEFTMENUHOME = 1402;
    public static final int TAPLEFTMENUNOTIFICATION = 1403;
    public static final int TAPLEFTMENUSCANQR = 1404;
    public static final int TAPLEFTMENUADDCASH = 1405;
    public static final int TAPLEFTMENUTRANSFERMONEY = 1406;
    public static final int TAPLEFTMENUADDCARD = 1407;
    public static final int TAPLEFTMENUTRANSACTIONLOGS = 1408;
    public static final int TAPLEFTMENUFAQ = 1409;
    public static final int TAPLEFTMENUHELP = 1410;
    public static final int TAPLEFTMENUABOUT = 1411;
    public static final int TAPLEFTMENULOGOUT = 1412;
    public static final int SCANQR_LAUNCH = 1500;
    public static final int SCANQR_ACCESSDENIED = 1501;
    public static final int SCANQR_NAVIGATEBACK = 1502;
    public static final int SCANQR_GETCODE = 1503;
    public static final int SCANQR_WRONGCODE = 1504;
    public static final int SCANQR_GETORDER = 1505;
    public static final int SCANQR_NOORDER = 1506;
    public static final int MONEYTRANSFER_LAUNCH = 1600;
    public static final int MONEYTRANSFER_NAVIGATEBACK = 1601;
    public static final int MONEYTRANSFER_CHOOSERECENTTRANSACTION = 1602;
    public static final int MONEYTRANSFER_INPUTSMALLAMOUNT = 1603;
    public static final int MONEYTRANSFER_INPUTBIGAMOUNT = 1604;
    public static final int MONEYTRANSFER_INPUTNODESCRIPTION = 1605;
    public static final int MONEYTRANSFER_INPUTDESCRIPTION = 1606;
    public static final int MONEYTRANSFER_TAPCONTINUE = 1607;
    public static final int MONEYTRANSFER_CHANGERECEIVER = 1608;
    public static final int ADDCASH_LAUNCH = 1700;
    public static final int ADDCASH_NAVIGATEBACK = 1701;
    public static final int ADDCASH_INPUTSMALLAMOUNT = 1702;
    public static final int ADDCASH_INPUTBIGAMOUNT = 1703;
    public static final int ADDCASH_INPUTWRONGAMOUNT = 1704;
    public static final int ADDCASH_INPUTAMOUNTOK = 1705;
    public static final int MANAGECARD_LAUNCH = 1800;
    public static final int MANAGECARD_NAVIGATEBACK = 1801;
    public static final int MANAGECARD_ADDCARD_LAUNCH = 1802;
    public static final int MANAGECARD_DELETECARD = 1803;
    public static final int MANAGECARD_TAPADDCARD = 1804;
    public static final int NOTIFICATIONS_LAUNCH = 1900;
    public static final int NOTIFICATIONS_NAVIGATEBACK = 1901;
    public static final int NOTIFICATIONS_TAPTRANSACTIONITEM = 1902;
    public static final int NOTIFICATIONS_TAPPROFILEITEM = 1903;
    public static final int NOTIFICATIONS_TAPPROMOTIONITEM = 1904;

    public static String actionFromEventId(int eventId) {
        switch (eventId) {
            case APP_LAUNCH:
                return "App_Launch";
            case TAP_LOGIN:
                return "Tap_Login";
            case LOGINFAILED_NONETWORK:
                return "LoginFailed_NoNetwork";
            case LOGINFAILED_USERDENIED:
                return "LoginFailed_UserDenied";
            case LOGINSUCCESS_ZALO:
                return "LoginSuccess_Zalo";
            case LOGINFAILED_API_ERROR:
                return "LoginFailed_API_Error";
            case NEEDINVITATIONCODE:
                return "NeedInvitationCode";
            case APPLAUNCHHOMEFROMLOGIN:
                return "AppLaunchHomeFromLogin";
            case INVITATIONFROMLOGIN:
                return "InvitationFromLogin";
            case INPUTINVITATIONCODE:
                return "InputInvitationCode";
            case INVITATIONCODEWRONG:
                return "InvitationCodeWrong";
            case INVITATIONCODEREUSED:
                return "InvitationCodeReused";
            case INVITATIONCODESUCCESS:
                return "InvitationCodeSuccess";
            case APPLAUNCHHOMEFROMINVITATION:
                return "AppLaunchHomeFromInvitation";
            case APPLAUNCHHOME:
                return "AppLaunchHome";
            case TAPSCANQR:
                return "TapScanQR";
            case TAPADDCASH:
                return "TapAddCash";
            case TAPMANAGECARDS:
                return "TapManageCards";
            case TAPMENUBUTTON:
                return "TapMenuButton";
            case TAPNOTIFICATIONBUTTON:
                return "TapNotificationButton";
            case TAPBANNERPOSITION1:
                return "TapBannerPosition1";
            case TAPBANNERPOSITION2:
                return "TapBannerPosition2";
            case TAPBANNERPOSITION3:
                return "TapBannerPosition3";
            case TAPAPPICON_1_1:
                return "TapAppIcon_1_1";
            case TAPAPPICON_1_2:
                return "TapAppIcon_1_2";
            case TAPAPPICON_1_3:
                return "TapAppIcon_1_3";
            case TAPAPPICON_1_4:
                return "TapAppIcon_1_4";
            case TAPAPPICON_2_1:
                return "TapAppIcon_2_1";
            case TAPAPPICON_2_2:
                return "TapAppIcon_2_2";
            case TAPAPPICON_2_3:
                return "TapAppIcon_2_3";
            case TAPAPPICON_2_4:
                return "TapAppIcon_2_4";
            case TAPAPPICON_3_1:
                return "TapAppIcon_3_1";
            case TAPAPPICON_3_2:
                return "TapAppIcon_3_2";
            case TAPAPPICON_3_3:
                return "TapAppIcon_3_3";
            case TAPAPPICON_3_4:
                return "TapAppIcon_3_4";
            case TAPAPPICON_4_1:
                return "TapAppIcon_4_1";
            case TAPAPPICON_4_2:
                return "TapAppIcon_4_2";
            case TAPAPPICON_4_3:
                return "TapAppIcon_4_3";
            case TAPAPPICON_4_4:
                return "TapAppIcon_4_4";
            case TAPAPPICON_5_1:
                return "TapAppIcon_5_1";
            case TAPAPPICON_5_2:
                return "TapAppIcon_5_2";
            case TAPAPPICON_5_3:
                return "TapAppIcon_5_3";
            case TAPAPPICON_5_4:
                return "TapAppIcon_5_4";
            case OPENLEFTMENU:
                return "OpenLeftMenu";
            case TAPLEFTMENUUSERPROFILE:
                return "TapLeftMenuUserProfile";
            case TAPLEFTMENUHOME:
                return "TapLeftMenuHome";
            case TAPLEFTMENUNOTIFICATION:
                return "TapLeftMenuNotification";
            case TAPLEFTMENUSCANQR:
                return "TapLeftMenuScanQR";
            case TAPLEFTMENUADDCASH:
                return "TapLeftMenuAddCash";
            case TAPLEFTMENUTRANSFERMONEY:
                return "TapLeftMenuTransferMoney";
            case TAPLEFTMENUADDCARD:
                return "TapLeftMenuAddCard";
            case TAPLEFTMENUTRANSACTIONLOGS:
                return "TapLeftMenuTransactionLogs";
            case TAPLEFTMENUFAQ:
                return "TapLeftMenuFAQ";
            case TAPLEFTMENUHELP:
                return "TapLeftMenuHelp";
            case TAPLEFTMENUABOUT:
                return "TapLeftMenuAbout";
            case TAPLEFTMENULOGOUT:
                return "TapLeftMenuLogout";
            case SCANQR_LAUNCH:
                return "ScanQR_Launch";
            case SCANQR_ACCESSDENIED:
                return "ScanQR_AccessDenied";
            case SCANQR_NAVIGATEBACK:
                return "ScanQR_NavigateBack";
            case SCANQR_GETCODE:
                return "ScanQR_GetCode";
            case SCANQR_WRONGCODE:
                return "ScanQR_WrongCode";
            case SCANQR_GETORDER:
                return "ScanQR_GetOrder";
            case SCANQR_NOORDER:
                return "ScanQR_NoOrder";
            case MONEYTRANSFER_LAUNCH:
                return "MoneyTransfer_Launch";
            case MONEYTRANSFER_NAVIGATEBACK:
                return "MoneyTransfer_NavigateBack";
            case MONEYTRANSFER_CHOOSERECENTTRANSACTION:
                return "MoneyTransfer_ChooseRecentTransaction";
            case MONEYTRANSFER_INPUTSMALLAMOUNT:
                return "MoneyTransfer_InputSmallAmount";
            case MONEYTRANSFER_INPUTBIGAMOUNT:
                return "MoneyTransfer_InputBigAmount";
            case MONEYTRANSFER_INPUTNODESCRIPTION:
                return "MoneyTransfer_InputNoDescription";
            case MONEYTRANSFER_INPUTDESCRIPTION:
                return "MoneyTransfer_InputDescription";
            case MONEYTRANSFER_TAPCONTINUE:
                return "MoneyTransfer_TapContinue";
            case MONEYTRANSFER_CHANGERECEIVER:
                return "MoneyTransfer_ChangeReceiver";
            case ADDCASH_LAUNCH:
                return "AddCash_Launch";
            case ADDCASH_NAVIGATEBACK:
                return "AddCash_NavigateBack";
            case ADDCASH_INPUTSMALLAMOUNT:
                return "AddCash_InputSmallAmount";
            case ADDCASH_INPUTBIGAMOUNT:
                return "AddCash_InputBigAmount";
            case ADDCASH_INPUTWRONGAMOUNT:
                return "AddCash_InputWrongAmount";
            case ADDCASH_INPUTAMOUNTOK:
                return "AddCash_InputAmountOK";
            case MANAGECARD_LAUNCH:
                return "ManageCard_Launch";
            case MANAGECARD_NAVIGATEBACK:
                return "ManageCard_NavigateBack";
            case MANAGECARD_ADDCARD_LAUNCH:
                return "ManageCard_AddCard_Launch";
            case MANAGECARD_DELETECARD:
                return "ManageCard_DeleteCard";
            case MANAGECARD_TAPADDCARD:
                return "ManageCard_TapAddCard";
            case NOTIFICATIONS_LAUNCH:
                return "Notifications_Launch";
            case NOTIFICATIONS_NAVIGATEBACK:
                return "Notifications_NavigateBack";
            case NOTIFICATIONS_TAPTRANSACTIONITEM:
                return "Notifications_TapTransactionItem";
            case NOTIFICATIONS_TAPPROFILEITEM:
                return "Notifications_TapProfileItem";
            case NOTIFICATIONS_TAPPROMOTIONITEM:
                return "Notifications_TapPromotionItem";
            default:
                return "DefaultAction";
        }
    }

    public static String categoryFromEventId(int eventId) {
        switch (eventId) {
            case APP_LAUNCH:
                return "Startup";
            case TAP_LOGIN:
                return "Login";
            case LOGINFAILED_NONETWORK:
                return "Login";
            case LOGINFAILED_USERDENIED:
                return "Login";
            case LOGINSUCCESS_ZALO:
                return "Login";
            case LOGINFAILED_API_ERROR:
                return "Login";
            case NEEDINVITATIONCODE:
                return "Login";
            case APPLAUNCHHOMEFROMLOGIN:
                return "Login";
            case INVITATIONFROMLOGIN:
                return "Login";
            case INPUTINVITATIONCODE:
                return "Invitation";
            case INVITATIONCODEWRONG:
                return "Invitation";
            case INVITATIONCODEREUSED:
                return "Invitation";
            case INVITATIONCODESUCCESS:
                return "Invitation";
            case APPLAUNCHHOMEFROMINVITATION:
                return "Invitation";
            case APPLAUNCHHOME:
                return "Home";
            case TAPSCANQR:
                return "Home";
            case TAPADDCASH:
                return "Home";
            case TAPMANAGECARDS:
                return "Home";
            case TAPMENUBUTTON:
                return "Home";
            case TAPNOTIFICATIONBUTTON:
                return "Home";
            case TAPBANNERPOSITION1:
                return "Home";
            case TAPBANNERPOSITION2:
                return "Home";
            case TAPBANNERPOSITION3:
                return "Home";
            case TAPAPPICON_1_1:
                return "Home";
            case TAPAPPICON_1_2:
                return "Home";
            case TAPAPPICON_1_3:
                return "Home";
            case TAPAPPICON_1_4:
                return "Home";
            case TAPAPPICON_2_1:
                return "Home";
            case TAPAPPICON_2_2:
                return "Home";
            case TAPAPPICON_2_3:
                return "Home";
            case TAPAPPICON_2_4:
                return "Home";
            case TAPAPPICON_3_1:
                return "Home";
            case TAPAPPICON_3_2:
                return "Home";
            case TAPAPPICON_3_3:
                return "Home";
            case TAPAPPICON_3_4:
                return "Home";
            case TAPAPPICON_4_1:
                return "Home";
            case TAPAPPICON_4_2:
                return "Home";
            case TAPAPPICON_4_3:
                return "Home";
            case TAPAPPICON_4_4:
                return "Home";
            case TAPAPPICON_5_1:
                return "Home";
            case TAPAPPICON_5_2:
                return "Home";
            case TAPAPPICON_5_3:
                return "Home";
            case TAPAPPICON_5_4:
                return "Home";
            case OPENLEFTMENU:
                return "Left Menu";
            case TAPLEFTMENUUSERPROFILE:
                return "Left Menu";
            case TAPLEFTMENUHOME:
                return "Left Menu";
            case TAPLEFTMENUNOTIFICATION:
                return "Left Menu";
            case TAPLEFTMENUSCANQR:
                return "Left Menu";
            case TAPLEFTMENUADDCASH:
                return "Left Menu";
            case TAPLEFTMENUTRANSFERMONEY:
                return "Left Menu";
            case TAPLEFTMENUADDCARD:
                return "Left Menu";
            case TAPLEFTMENUTRANSACTIONLOGS:
                return "Left Menu";
            case TAPLEFTMENUFAQ:
                return "Left Menu";
            case TAPLEFTMENUHELP:
                return "Left Menu";
            case TAPLEFTMENUABOUT:
                return "Left Menu";
            case TAPLEFTMENULOGOUT:
                return "Left Menu";
            case SCANQR_LAUNCH:
                return "Scan QR";
            case SCANQR_ACCESSDENIED:
                return "Scan QR";
            case SCANQR_NAVIGATEBACK:
                return "Scan QR";
            case SCANQR_GETCODE:
                return "Scan QR";
            case SCANQR_WRONGCODE:
                return "Scan QR";
            case SCANQR_GETORDER:
                return "Scan QR";
            case SCANQR_NOORDER:
                return "Scan QR";
            case MONEYTRANSFER_LAUNCH:
                return "Money Transfer";
            case MONEYTRANSFER_NAVIGATEBACK:
                return "Money Transfer";
            case MONEYTRANSFER_CHOOSERECENTTRANSACTION:
                return "Money Transfer";
            case MONEYTRANSFER_INPUTSMALLAMOUNT:
                return "Money Transfer";
            case MONEYTRANSFER_INPUTBIGAMOUNT:
                return "Money Transfer";
            case MONEYTRANSFER_INPUTNODESCRIPTION:
                return "Money Transfer";
            case MONEYTRANSFER_INPUTDESCRIPTION:
                return "Money Transfer";
            case MONEYTRANSFER_TAPCONTINUE:
                return "Money Transfer";
            case MONEYTRANSFER_CHANGERECEIVER:
                return "Money Transfer";
            case ADDCASH_LAUNCH:
                return "Add Cash";
            case ADDCASH_NAVIGATEBACK:
                return "Add Cash";
            case ADDCASH_INPUTSMALLAMOUNT:
                return "Add Cash";
            case ADDCASH_INPUTBIGAMOUNT:
                return "Add Cash";
            case ADDCASH_INPUTWRONGAMOUNT:
                return "Add Cash";
            case ADDCASH_INPUTAMOUNTOK:
                return "Add Cash";
            case MANAGECARD_LAUNCH:
                return "Manage Card";
            case MANAGECARD_NAVIGATEBACK:
                return "Manage Card";
            case MANAGECARD_ADDCARD_LAUNCH:
                return "Manage Card";
            case MANAGECARD_DELETECARD:
                return "Manage Card";
            case MANAGECARD_TAPADDCARD:
                return "Manage Card";
            case NOTIFICATIONS_LAUNCH:
                return "Notifications";
            case NOTIFICATIONS_NAVIGATEBACK:
                return "Notifications";
            case NOTIFICATIONS_TAPTRANSACTIONITEM:
                return "Notifications";
            case NOTIFICATIONS_TAPPROFILEITEM:
                return "Notifications";
            case NOTIFICATIONS_TAPPROMOTIONITEM:
                return "Notifications";
            default:
                return "DefaultCategory";
        }
    }
}

