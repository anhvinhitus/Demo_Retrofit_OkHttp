/*

package vn.com.vng.zalopay.domain.interactor;


import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import vn.com.vng.zalopay.domain.executor.PostExecutionThread;
import vn.com.vng.zalopay.domain.executor.ThreadExecutor;


public abstract class UseCase {

    private final ThreadExecutor threadExecutor;
    private final PostExecutionThread postExecutionThread;

    // private Subscription subscription = Subscriptions.empty();
    private final CompositeSubscription subscription = new CompositeSubscription();

    protected UseCase(ThreadExecutor threadExecutor,
                      PostExecutionThread postExecutionThread) {
        this.threadExecutor = threadExecutor;
        this.postExecutionThread = postExecutionThread;
    }


    protected abstract Observable buildUseCaseObservable();

    //  protected abstract Observable buildUseCaseObservable(@Nullable T... params);

    @SuppressWarnings("unchecked")
    public void execute(Subscriber UseCaseSubscriber) {
        Subscription sub = this.buildUseCaseObservable()
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.getScheduler())
                .subscribe(UseCaseSubscriber);
        subscription.add(sub);
    }

    public void unsubscribe() {
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
*/
