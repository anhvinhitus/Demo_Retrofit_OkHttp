package vn.com.vng.zalopay.data.util;

import java.util.ArrayList;
import java.util.List;

import vn.com.vng.zalopay.domain.model.AppResource;

import static vn.com.vng.zalopay.data.util.Lists.isEmptyOrNull;

public class ListStringUtil {

    public static String toString(long[] list) {
        if (isEmptyOrNull(list)) return "";

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            str.append(str.length() == 0 ? String.valueOf(list[i]) : "," + String.valueOf(list[i]));
        }
        return str.toString();
    }

    public static String toString(long[] list1, long[] list2, long[] list3) {
        String appIds = "";
        if (list1 != null) {
            appIds = ListStringUtil.toString(list1);
        }
        if (list2 != null) {
            String ids = ListStringUtil.toString(list2);
            if (ids.length() > 0) {
                appIds = appIds.length() == 0 ? ids : appIds + "," + ids;
            }
        }
        if (list3 != null) {
            String ids = ListStringUtil.toString(list3);
            if (ids.length() > 0) {
                appIds = appIds.length() == 0 ? ids : appIds + "," + ids;
            }
        }
        return appIds;
    }

    public static String toString(List<Long> list) {
        if (isEmptyOrNull(list)) return "";
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            str.append(str.length() == 0 ? String.valueOf(list.get(i)) : "," + String.valueOf(list.get(i)));
        }
        return str.toString();
    }

    public static String toStringListInt(List<Integer> list) {
        if (isEmptyOrNull(list)) return "";
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            str.append(str.length() == 0 ? String.valueOf(list.get(i)) : "," + String.valueOf(list.get(i)));
        }
        return str.toString();
    }


    public static String listStringtoString(List<String> list) {
        if (isEmptyOrNull(list)) return "";

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            str.append(str.length() == 0 ? list.get(i) : "," + list.get(i));
        }
        return str.toString();
    }

    public static String toString(List<Long> list1, List<Long> list2, List<Long> list3, List<Long> list4) {
        StringBuilder appIds = new StringBuilder();

        if (list1 != null) {
            appIds.append(ListStringUtil.toString(list1));
        }

        if (list2 != null) {
            String ids = ListStringUtil.toString(list2);
            if (ids.length() > 0) {
                appIds.append(appIds.length() == 0 ? ids : "," + ids);
            }
        }

        if (list3 != null) {
            String ids = ListStringUtil.toString(list3);
            if (ids.length() > 0) {
                appIds.append(appIds.length() == 0 ? ids : "," + ids);
            }
        }


        if (list4 != null) {
            String ids = ListStringUtil.toString(list4);
            if (ids.length() > 0) {
                appIds.append(appIds.length() == 0 ? ids : "," + ids);
            }
        }
        return appIds.toString();
    }


    public static String toString(List<Long> list1, List<Long> list2) {
        StringBuilder appIds = new StringBuilder();
        if (list1 != null) {
            appIds.append(ListStringUtil.toString(list1));
        }

        if (list2 != null) {
            String ids = ListStringUtil.toString(list2);
            if (ids.length() > 0) {
                appIds.append(appIds.length() == 0 ? ids : "," + ids);
            }
        }
        return appIds.toString();
    }

    public static long[] toLongArr(String longStr) {
        String[] strArr = longStr.split(",");
        long[] longArr = new long[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            String str = strArr[i];
            try {
                long longV = Long.parseLong(str);
                longArr[i] = longV;
            } catch (NumberFormatException ex) {
                //empty
            }
        }
        return longArr;
    }

    public static List<Long> toListLong(String longStr) {
        String[] strArr = longStr.split(",");
        List<Long> longArr = new ArrayList<>();
        for (int i = 0; i < strArr.length; i++) {
            String str = strArr[i];
            try {
                long longV = Long.parseLong(str);
                longArr.add(longV);
            } catch (NumberFormatException ex) {
            }
        }
        return longArr;
    }


}
