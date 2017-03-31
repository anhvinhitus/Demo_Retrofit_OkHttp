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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Scheduler;
import vn.com.vng.zalopay.data.Constants;
import vn.com.vng.zalopay.network.API_NAME;
import vn.com.vng.zalopay.network.RETRY;

public final class RxJavaCallAdapterFactory extends CallAdapter.Factory {
    public enum AdapterType {
        ZaloPay,
        RedPacket,
        React,
    }

    /**
     * TODO
     */
    public static RxJavaCallAdapterFactory create(Context context, AdapterType adapterType) {
        return new RxJavaCallAdapterFactory(null, context, adapterType);
    }

    /**
     * TODO
     */
    public static RxJavaCallAdapterFactory createWithScheduler(Scheduler scheduler, Context context, AdapterType adapterType) {
        if (scheduler == null) throw new NullPointerException("scheduler == null");
        return new RxJavaCallAdapterFactory(scheduler, context, adapterType);
    }

    private final Scheduler scheduler;
    private Context mApplicationContext;
    private final AdapterType mAdapterType;

    private RxJavaCallAdapterFactory(Scheduler scheduler, Context context, AdapterType adapterType) {
        this.scheduler = scheduler;
        this.mApplicationContext = context;
        this.mAdapterType = adapterType;
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        int apiEventId = -1;
        int maxRetries = Constants.NUMBER_RETRY_REST;

        for (Annotation annotation : annotations) {

            if (annotation instanceof API_NAME) {
                apiEventId = ((API_NAME) annotation).value();
                continue;
            }

            if (annotation instanceof RETRY) {
                maxRetries = ((RETRY) annotation).value();
            }
        }

        return getCallAdapter(returnType, scheduler, apiEventId, maxRetries);
    }

    private CallAdapter<Observable<?>> getCallAdapter(Type returnType, Scheduler scheduler, int apiEventId, int maxRetries) {
        Class<?> rawType = getRawType(returnType);
        if (rawType != Observable.class) {
            return null;
        }

        Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
        switch (mAdapterType) {
            case ZaloPay:
                return new ZaloPayCallAdapter(mApplicationContext, apiEventId, observableType, scheduler);
            case RedPacket:
                return new RedPacketCallAdapter(mApplicationContext, apiEventId, observableType, scheduler);
            case React:
                return new ReactNativeCallAdapter(mApplicationContext, apiEventId, observableType, scheduler, maxRetries);
            default:
                return new ZaloPayCallAdapter(mApplicationContext, apiEventId, observableType, scheduler);
        }
    }
}
