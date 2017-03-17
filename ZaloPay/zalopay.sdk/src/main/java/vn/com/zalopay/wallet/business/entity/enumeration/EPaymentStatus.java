package vn.com.zalopay.wallet.business.entity.enumeration;

public enum EPaymentStatus {
    /**
     * Transaction is processing
     */
    ZPC_TRANXSTATUS_PROCESSING(0),
    /**
     * Transaction success(confirmed by application's server).
     */
    ZPC_TRANXSTATUS_SUCCESS(1),
    /**
     * Transaction failed.
     */
    ZPC_TRANXSTATUS_FAIL(-1),
    /**
     * MONEY NOT ENOUGH IN WALLET.
     */
    ZPC_TRANXSTATUS_MONEY_NOT_ENOUGH(-2),
    /**
     * TOKEN INVALID
     */
    ZPC_TRANXSTATUS_TOKEN_INVALID(-3),
    /***
     * PARAMS INVALID.
     */
    ZPC_TRANXSTATUS_INPUT_INVALID(-4),
    /***
     * CLOSE MERGANT'S SCREEN.
     */
    ZPC_TRANXSTATUS_CLOSE(-5),
    /***
     * ZALOPAY ACCOUNT IS LOCKED
     */
    ZPC_TRANXSTATUS_LOCK_USER(-6),
    /***
     * MERCHANT NEED TO UPGRADE LEVEL
     */
    ZPC_TRANXSTATUS_UPGRADE(6),
    /***
     * MERCHANT NEED TO UPGRADE LEVEL AND SAVE CARD LATER.
     */
    ZPC_TRANXSTATUS_UPGRADE_SAVECARD(7),
    /***
     * MERCHANT NEED TO UPGRADE LEVEL AND SAVE CARD LATER.
     */
    ZPC_TRANXSTATUS_NO_INTERNET(8),
    /***
     * MERCHANT NEED TO SHOW SERVICE MAINTENANCE
     */
    ZPC_TRANXSTATUS_SERVICE_MAINTENANCE(9),
    /***
     * MERCHANT NEED TO SHOW DIALOG AND REDIRECT USER TO NEW APP ON STORE.
     */
    ZPC_UPVERSION(10),
    /***
     * MERCHANT NEED TO REDIRECT USER TO LINK CARD
     */
    ZPC_TRANXSTATUS_NEED_LINKCARD(11),
    /***
     * EXCEPTION CASE FOR BIDV BANK
     * MERCHANT NEED TO DIRECT USER TO LINK CARD,
     * THEN USER LINKCARD SUCCESS REDIRECT USER TO PAYMENT AGAIN
     */
    ZPC_TRANXSTATUS_NEED_LINKCARD_BEFORE_PAYMENT(12),
    /***
     * LINK ACCOUNT
     */
    ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT(13),
    /***
     * EXCEPTION CASE FOR VIETCOMBANK BANK
     * MERCHANT NEED TO DIRECT USER TO LINK ACCOUNT,
     * THEN USER LINKCARD SUCCESS REDIRECT USER TO PAYMENT AGAIN
     */
    ZPC_TRANXSTATUS_NEED_LINK_ACCOUNT_BEFORE_PAYMENT(14),
    /***
     * EXCEPTION CASE FOR PAYMENT WHEN 1 LEVEL USER INPUT OR SELECT BANK ACCOUNT
     * USER NEED TO UP LEVEL , THEN LINK BANK ACCOUNT , THEN PAYMENT AGAIN
     */
    ZPC_TRANXSTATUS_UPLEVEL_AND_LINK_BANKACCOUNT_CONTINUE_PAYMENT(15),
    /***
     * MERCHANT NEED TO UPGRADE LEVEL 3
     */
    ZPC_TRANXSTATUS_UPGRADE_CMND_EMAIL(16);


    private int mNum = -1;

    private EPaymentStatus(int pNum) {
        this.mNum = (byte) pNum;
    }

    public static EPaymentStatus fromInt(int pNum) {
        if (pNum < 0)
            return EPaymentStatus.ZPC_TRANXSTATUS_FAIL;

        byte num = (byte) pNum;

        for (EPaymentStatus status : EPaymentStatus.values()) {
            if (status.mNum == num)
                return status;
        }
        return null;
    }

    public int getNum() {
        return mNum;
    }
}
