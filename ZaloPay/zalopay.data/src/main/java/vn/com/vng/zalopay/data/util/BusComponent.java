package vn.com.vng.zalopay.data.util;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.lang.annotation.Retention;
import java.util.HashMap;
import java.util.Map;

import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Used for subscribing to and publishing to subjects. Allowing you to send data between activities, fragments, etc.
 * <p>
 * Created by Pierce Zaifman on 2017-01-02.
 */

public final class BusComponent {

    private static SparseArray<PublishSubject<Object>> sSubjectMap = new SparseArray<>();
    private static Map<Object, CompositeSubscription> sSubscriptionsMap = new HashMap<>();

    public static final int APP_SUBJECT = 0;
    public static final int SUBJECT_ANOTHER_SUBJECT = 1;

    @Retention(SOURCE)
    @IntDef({APP_SUBJECT, SUBJECT_ANOTHER_SUBJECT})
    @interface Subject {
    }

    private BusComponent() {
        // hidden constructor
    }

    /**
     * Get the subject or create it if it's not already in memory.
     */
    @NonNull
    private static PublishSubject<Object> getSubject(@Subject int subjectCode) {
        PublishSubject<Object> subject = sSubjectMap.get(subjectCode);
        if (subject == null) {
            subject = PublishSubject.create();
            sSubjectMap.put(subjectCode, subject);
        }

        return subject;
    }

    /**
     * Get the CompositeSubscription or create it if it's not already in memory.
     */
    @NonNull
    private static CompositeSubscription getCompositeSubscription(@NonNull Object object) {
        CompositeSubscription compositeSubscription = sSubscriptionsMap.get(object);
        if (compositeSubscription == null) {
            compositeSubscription = new CompositeSubscription();
            sSubscriptionsMap.put(object, compositeSubscription);
        }

        return compositeSubscription;
    }

    /**
     * Subscribe to the specified subject and listen for updates on that subject. Pass in an object to associate
     * your registration with, so that you can unsubscribe later.
     * <br/><br/>
     * <b>Note:</b> Make sure to call {@link BusComponent#unregister(Object)} to avoid memory leaks.
     */
    public static void subscribe(@Subject int subject, @NonNull Object lifecycle, @NonNull Subscriber<Object> action, Scheduler scheduler) {
        Subscription subscription = getSubject(subject)
                .observeOn(scheduler)
                .subscribe(action);
        getCompositeSubscription(lifecycle).add(subscription);
    }

    /**
     * Unregisters this object from the bus, removing all subscriptions.
     * This should be called when the object is going to go out of memory.
     */
    public static void unregister(@NonNull Object lifecycle) {
        //We have to remove the composition from the map, because once you unsubscribe it can't be used anymore
        CompositeSubscription compositeSubscription = sSubscriptionsMap.remove(lifecycle);
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
    }

    /**
     * Publish an object to the specified subject for all subscribers of that subject.
     */
    public static void publish(@Subject int subject, @NonNull Object message) {
        PublishSubject<Object> publishSubject = getSubject(subject);
        if (publishSubject.hasObservers()) {
            publishSubject.onNext(message);
        }
    }
}