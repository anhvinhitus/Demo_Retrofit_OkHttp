package vn.com.zalopay.wallet.constants;

public class CardTypeUtils {
    @CardType
    public static String fromBankCode(String pBankCode) {
        switch (pBankCode) {
            case CardType.VISA:
                return CardType.VISA;
            case CardType.MASTER:
                return CardType.MASTER;
            case CardType.JCB:
                return CardType.JCB;
            case CardType.PVTB:
                return CardType.PVTB;
            case CardType.PBIDV:
                return CardType.PBIDV;
            case CardType.PSCB:
                return CardType.PSCB;
            case CardType.PSGCB:
                return CardType.PSGCB;
            case CardType.PVCB:
                return CardType.PVCB;
            default:
                return CardType.UNDEFINE;
        }
    }

    public static boolean isBankAccount(@CardType String pCardType) {
        if (pCardType == CardType.PVCB) {
            return true;
        }
        return false;
    }
}
