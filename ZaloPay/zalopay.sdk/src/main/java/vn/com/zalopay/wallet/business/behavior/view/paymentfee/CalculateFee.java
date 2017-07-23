package vn.com.zalopay.wallet.business.behavior.view.paymentfee;

import vn.com.zalopay.wallet.objectmanager.SingletonBase;

public class CalculateFee extends SingletonBase {
    private ICalculateFee mCalculator;
    public CalculateFee(){
        super();
    }

    public static CalculateFee newInstance() {
        return new CalculateFee();
    }

    public CalculateFee setCalculator(ICalculateFee pCalculator) {
        this.mCalculator = pCalculator;
        return this;
    }

    public double calculate(long amount) {
        return mCalculator.calculateFee(amount);
    }
}
