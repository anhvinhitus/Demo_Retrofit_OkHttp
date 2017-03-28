package vn.com.zalopay.wallet.business.entity.enumeration;

/**
 * Created by Duke on 3/28/17.
 */

public enum ESuggestActionType {
    UPDATE_INFO_DISPLAY(new int[]{1}),
    SUPPORT_DISPLAY(new int[]{2}),
    UPDATE_INFO_ABOVE(new int[]{1,2}),
    SUPPORT_ABOVE(new int[]{2,1});

    public int[] getValue() {
        return value;
    }

    private int[] value;

    ESuggestActionType(int[] ints) {
        value = ints;
    }
}
