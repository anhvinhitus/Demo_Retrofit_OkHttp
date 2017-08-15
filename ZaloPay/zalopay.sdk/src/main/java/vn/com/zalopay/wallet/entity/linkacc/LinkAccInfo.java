package vn.com.zalopay.wallet.entity.linkacc;

import vn.com.zalopay.wallet.entity.enumeration.ELinkAccType;

/*
 * Created by SinhTT on 1/19/17.
 */

public class LinkAccInfo {
    private String mBankCode;
    private ELinkAccType mLinkAccType;

    public LinkAccInfo(String pBankCode, ELinkAccType pLinkAccType) {
        mBankCode = pBankCode;
        mLinkAccType = pLinkAccType;
    }

    public String getBankCode() {
        return mBankCode;
    }

    public boolean isLinkAcc() {
        return mLinkAccType == ELinkAccType.LINK;
    }

    public boolean isUnlinkAcc() {
        return mLinkAccType == ELinkAccType.UNLINK;
    }
}
