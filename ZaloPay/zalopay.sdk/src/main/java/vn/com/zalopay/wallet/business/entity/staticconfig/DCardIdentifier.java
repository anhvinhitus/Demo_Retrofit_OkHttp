package vn.com.zalopay.wallet.business.entity.staticconfig;

import vn.com.zalopay.wallet.business.entity.base.BaseEntity;

public class DCardIdentifier extends BaseEntity<DCardIdentifier> {
    public String code;
    public String name;
    public String startPin;
    public int min_length;
    public int max_length;

    public boolean isMatchMaxLengthCard(int pLength) {
        if (max_length > 0 && pLength == max_length)
            return true;

        return false;
    }
}
