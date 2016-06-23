/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vn.com.vng.zalopay.data.net.adapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;
import vn.com.vng.zalopay.data.api.response.BaseResponse;
import vn.com.vng.zalopay.data.eventbus.ServerMaintainEvent;
import vn.com.vng.zalopay.data.eventbus.TokenExpiredEvent;
import vn.com.vng.zalopay.data.exception.BodyException;
import vn.com.vng.zalopay.data.exception.NetworkConnectionException;
import vn.com.vng.zalopay.data.exception.ServerMaintainException;
import vn.com.vng.zalopay.data.exception.TokenException;
import vn.com.vng.zalopay.data.ws.model.Event;

public final class CustomRxJavaCallAdapterFactory extends CallAdapter.Factory {
    /**
     * TODO
     */
    public static CustomRxJavaCallAdapterFactory create(Context context) {
        return new CustomRxJavaCallAdapterFactory(null, context);
    }

    /**
     * TODO
     */
    public static CustomRxJavaCallAdapterFactory createWithScheduler(Scheduler scheduler, Context context) {
        if (scheduler == null) throw new NullPointerException("scheduler == null");
        return new CustomRxJavaCallAdapterFactory(scheduler, context);
    }

    private final Scheduler scheduler;

    public static Context applicationContext;

    private CustomRxJavaCallAdapterFactory(Scheduler scheduler, Context context) {
        this.scheduler = scheduler;
        this.applicationContext = context;
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        CallAdapter<Observable<?>> callAdapter = getCallAdapter(returnType, scheduler);
        return callAdapter;
    }

    private CallAdapter<Observable<?>> getCallAdapter(Type returnType, Scheduler scheduler) {
        Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);

        return new SimpleCallAdapter(observableType, scheduler);
    }

    static final class CallOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
        private final Call<T> originalCall;

        CallOnSubscribe(Call<T> originalCall) {
            this.originalCall = originalCall;
        }

        @Override
        public void call(final Subscriber<? super Response<T>> subscriber) {
            // Since Call is a one-shot type, clone it for each new subscriber.
            final Call<T> call = originalCall.clone();

            // Attempt to cancel the call if it is still in-flight on unsubscription.
            subscriber.add(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    call.cancel();
                }
            }));

            try {
                Response<T> response = call.execute();
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(response);
                }
            } catch (Throwable t) {
                Exceptions.throwIfFatal(t);
                if (!subscriber.isUnsubscribed()) {
                    try {
                        if (isNetworkAvailable(applicationContext)) {
                            subscriber.onError(t);
                        } else {
                            subscriber.onError(new NetworkConnectionException());
                        }
                    } catch (Exception ex) {
                        Timber.w(ex, "Exception OnError :");
                    }
                }
                return;
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }

        private boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }


    static final class SimpleCallAdapter implements CallAdapter<Observable<?>> {
        private final Type responseType;
        private final Scheduler scheduler;

        SimpleCallAdapter(Type responseType, Scheduler scheduler) {
            this.responseType = responseType;
            this.scheduler = scheduler;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public <R> Observable<R> adapt(Call<R> call) {
            Observable<R> observable = Observable.create(new CallOnSubscribe<>(call))
                    .flatMap(response -> {
                        if (response.isSuccessful()) {
                            R body = response.body();
                            if (body instanceof BaseResponse) {
                                BaseResponse baseResponse = (BaseResponse) body;
                                if (baseResponse.isSuccessfulResponse()) {
                                    return Observable.just(body);
                                } else {
                                    if (baseResponse.isSessionExpired()) {
                                        EventBus.getDefault().post(new TokenExpiredEvent(baseResponse.err));
                                        return Observable.error(new TokenException());
                                    } else if (baseResponse.isServerMaintain()) {
                                        EventBus.getDefault().post(new ServerMaintainEvent());
                                        return Observable.error(new ServerMaintainException());
                                    } else {
                                        return Observable.error(new BodyException(((BaseResponse) body).err, ((BaseResponse) body).message));
                                    }
                                }
                            } else {
                                return Observable.just(body);
                            }
                        }

                        return Observable.error(new HttpException(response));
                    });

            if (scheduler != null) {
                return observable.subscribeOn(scheduler);
            }
            return observable;
        }
    }

}
