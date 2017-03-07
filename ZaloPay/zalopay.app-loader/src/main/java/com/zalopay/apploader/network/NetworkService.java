package com.zalopay.apploader.network;

import com.facebook.react.bridge.ReadableMap;

import rx.Observable;

/**
 * Created by AnhHieu on 9/20/16.
 * *
 */
public interface NetworkService {
    Observable<String> request(String baseUrl, ReadableMap rawContent);
}
