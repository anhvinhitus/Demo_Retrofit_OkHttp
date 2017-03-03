package vn.com.zalopay.wallet.business.entity.base;

public class DMapCardResult {
    protected String cardLogo;
    protected String last4Number;
    protected String bankName;

    public String getCardLogo() {
        return cardLogo;
    }

    public void setCardLogo(String cardLogo) {
        this.cardLogo = cardLogo;
    }

    public String getLast4Number() {
        return last4Number;
    }

    public void setLast4Number(String last4Number) {
        this.last4Number = last4Number;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
