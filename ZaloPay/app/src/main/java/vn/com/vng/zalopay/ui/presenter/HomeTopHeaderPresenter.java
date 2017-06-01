package vn.com.vng.zalopay.ui.presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;
import vn.com.vng.zalopay.data.eventbus.NotificationChangeEvent;
import vn.com.vng.zalopay.data.eventbus.ReadNotifyEvent;
import vn.com.vng.zalopay.data.notification.NotificationStore;
import vn.com.vng.zalopay.domain.interactor.DefaultSubscriber;
import vn.com.vng.zalopay.ui.view.IHomeTopHeaderView;

/**
 * Created by Duke on 5/11/17.
 * Handle top header event and data
 */

public class HomeTopHeaderPresenter extends AbstractPresenter<IHomeTopHeaderView> {
    private EventBus mEventBus;
    private NotificationStore.Repository mNotificationRepository;

    @Inject
    HomeTopHeaderPresenter(EventBus eventBus,
                     NotificationStore.Repository notificationRepository) {
        this.mEventBus = eventBus;
        this.mNotificationRepository = notificationRepository;
    }

    @Override
    public void attachView(IHomeTopHeaderView o) {
        super.attachView(o);
        registerEvent();
    }

    private void registerEvent() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    private void unregisterEvent() {
        mEventBus.unregister(this);
    }

    @Override
    public void detachView() {
        unregisterEvent();
        super.detachView();
    }

    /*
    * Local functions
    * */
    public void initialize() {
        getTotalNotification(100);
    }

    // because of delay, subscriber at startup is sometime got triggered after the immediate subscriber
    // when received notification
    private void getTotalNotification(long delay) {
        Subscription subscription = mNotificationRepository.totalNotificationUnRead()
                .delaySubscription(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new HomeTopHeaderPresenter.NotificationSubscriber());
        mSubscription.add(subscription);
    }

    /*
    * Event bus
    * */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onNotificationUpdated(NotificationChangeEvent event) {
        Timber.d("on Notification updated state %s", event.isRead());
        if (!event.isRead()) {
            getTotalNotification(0);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onReadNotify(ReadNotifyEvent event) {
        Timber.d("onReadNotify");
        getTotalNotification(0);
    }

    /*
    * Custom subscribers
    * */
    private final class NotificationSubscriber extends DefaultSubscriber<Integer> {
        @Override
        public void onNext(Integer integer) {
            Timber.d("Got total %s unread notification messages", integer);
            if (mView != null) {
                mView.setTotalNotify(integer);
            }
        }
    }
}