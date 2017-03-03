package vn.com.zalopay.wallet.business.behavior.view.paymentfee;

public class CBaseCalculateFee {
    private static CBaseCalculateFee _object;
    private ICalculateFee mCalculator;

    public static CBaseCalculateFee getInstance() {
        if (CBaseCalculateFee._object == null)
            CBaseCalculateFee._object = new CBaseCalculateFee();

        return CBaseCalculateFee._object;
    }

    public CBaseCalculateFee setCalculator(ICalculateFee pCalculator) {
        this.mCalculator = pCalculator;

        return this;
    }

    public double countFee() {
        return mCalculator.calculateFee();
    }
}
