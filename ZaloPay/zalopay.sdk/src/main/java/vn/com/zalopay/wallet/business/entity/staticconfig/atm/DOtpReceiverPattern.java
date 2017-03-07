package vn.com.zalopay.wallet.business.entity.staticconfig.atm;


public class DOtpReceiverPattern {
    public String sender;
    public String bankcode;
    public int start;
    public int length;
    public boolean begin = false;//true read sms from the begining of content.
    public boolean isdigit = true;//otp is all digital
}
