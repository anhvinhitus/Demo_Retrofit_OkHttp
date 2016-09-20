package com.zalopay.apploader.network;

import rx.Observable;

/**
 * Created by AnhHieu on 9/20/16.
 * *
 */
public interface NetworkService {
    Observable<String> request(String baseUrl, String rawContent);
}
