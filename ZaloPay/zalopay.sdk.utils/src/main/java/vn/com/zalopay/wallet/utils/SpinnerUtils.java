package vn.com.zalopay.wallet.utils;

import android.widget.Spinner;

import java.util.ArrayList;

/**
 * Created by SinhTT on 10/11/2016.
 */

public class SpinnerUtils {
    public static String[] getItems(Spinner pView) {
        if (pView != null) {
            ArrayList<String> arrays = new ArrayList();
            for (int i = 0; i < pView.getCount(); i++) {
                arrays.add(pView.getAdapter().getItem(i).toString());
            }
            return arrays.toArray(new String[pView.getCount()]);
        }

        return null;
    }
}
