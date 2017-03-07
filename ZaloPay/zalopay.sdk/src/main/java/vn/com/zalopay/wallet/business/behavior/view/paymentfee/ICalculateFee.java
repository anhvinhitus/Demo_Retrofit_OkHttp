package vn.com.zalopay.wallet.business.behavior.view.paymentfee;

/***
 * use strategy pattern for calculating each payment channel's fee
 * idea: separate algorithrm(somethings maybe change later) from class.
 * easy maintenance,easy edit later.
 */
public interface ICalculateFee {
    double calculateFee();
}
