package vn.com.vng.zalopay.banner.model;

/**
 * Created by longlv on 29/08/2016.
 *
 */
public enum  BannerInternalFunction {
    Deposit(1), WithDraw(2), SaveCard(3), Pay(4), TransferMoney(5), RedPacket(6);

    int value;

    BannerInternalFunction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
