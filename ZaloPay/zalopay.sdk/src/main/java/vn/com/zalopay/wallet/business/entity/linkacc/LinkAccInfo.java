package vn.com.zalopay.wallet.business.entity.linkacc;

import vn.com.zalopay.wallet.business.entity.enumeration.ELinkAccType;

/**
 * Created by cpu11843-local on 1/19/17.
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

    public ELinkAccType getLinkAccType() {
        return mLinkAccType;
    }

    public boolean isLinkAcc() {
        return mLinkAccType == ELinkAccType.LINK;
    }

    public boolean isUnlinkAcc() {
        return mLinkAccType == ELinkAccType.UNLINK;
    }
}
