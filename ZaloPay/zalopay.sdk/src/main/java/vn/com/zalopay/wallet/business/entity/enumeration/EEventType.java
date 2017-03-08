package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EEventType {

    ON_CLICK,                // Click back or exit button
    ON_GET_STATUS_COMPLETE,
    ON_CHECK_STATUS_SUBMIT_COMPLETE,
    ON_SUBMIT_ORDER_COMPLETED,
    ON_VERIFY_MAPCARD_COMPLETE,
    ON_GET_CARDINFO_LIST_COMPLETE,
    ON_GET_BANKACCOUNT_LIST_COMPLETE,
    ON_SAVE_CARD,
    ON_PAYMENT_RESULT_BROWSER,
    ON_ATM_AUTHEN_PAYER_COMPLETE,
    ON_FAIL,
    ON_PAYMENT_COMPLETED,
    ON_REQUIRE_RENDER,
    ON_LOADSITE_ERROR,
    ON_BACK_WHEN_LOADSITE,
    ON_START,                // start vcb link
    ON_PROGRESSING,
    ON_HIT,
    ON_SUBMIT_LINKACC_COMPLETED,
    ON_NOTIFY_BANKACCOUNT,
}
