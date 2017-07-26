package vn.com.zalopay.wallet.business.entity.staticconfig;

import vn.com.zalopay.wallet.business.entity.base.BaseEntity;

public class CardRule extends BaseEntity<CardRule> {
    public String code;
    public String name;
    public String startPin;
    public int min_length;
    public int max_length;

    public boolean isMatchMaxLengthCard(int pLength) {
        return max_length > 0 && pLength == max_length;

    }
}
