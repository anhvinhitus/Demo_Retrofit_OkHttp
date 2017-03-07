package vn.com.zalopay.wallet.business.entity.enumeration;

public enum ECardFlowType {
    LOADWEB(1),
    PARSEWEB(2),
    API(3);

    private int name;

    private ECardFlowType(int s) {
        name = s;
    }
}
