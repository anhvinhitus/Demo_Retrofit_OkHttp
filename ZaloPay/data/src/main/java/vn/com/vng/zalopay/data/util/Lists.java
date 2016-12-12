package vn.com.vng.zalopay.data.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

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

    public static <T, R> List<R> transform(List<T> list, Func1<? super T, ? extends R> converter) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<R> transformedList = new ArrayList<>(list.size());
        for (T t : list) {
            R r = converter.call(t);
            if (r == null) {
                continue;
            }

            transformedList.add(r);
        }

        return transformedList;
    }
}
