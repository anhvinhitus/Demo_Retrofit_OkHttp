package vn.com.vng.zalopay.data.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.functions.Func1;
import rx.functions.Func2;

public final class Lists {

    private Lists() {
    }

    public static <T> boolean elementsEqual(List<T> list1, List<T> list2,
                                            Func2<T, T, Boolean> comparator) {
        if (list1 == null || list2 == null) {
            return false;
        }

        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            if (!comparator.call(list1.get(i), list2.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static <T> boolean isEmptyOrNull(Collection<T> list) {
        return list == null || list.isEmpty();
    }

    public static <T> boolean isEmptyOrNull(long[] list) {
        return list == null || list.length == 0;
    }

    public static <T1, T2> List<T2> map(List<T1> list, Func1<T1, T2> converter) {
        ArrayList<T2> mappedList = new ArrayList<>(list.size());
        for (T1 item : list) {
            mappedList.add(converter.call(item));
        }

        return mappedList;
    }
}
