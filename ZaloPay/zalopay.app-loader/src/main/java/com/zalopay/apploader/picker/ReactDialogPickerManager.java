/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.zalopay.apploader.picker;

import android.content.Context;
import android.widget.Spinner;

import com.facebook.react.uimanager.ThemedReactContext;
import com.zalopay.apploader.ReactNativeHostable;

import timber.log.Timber;

/**
 * {@link ReactPickerManager} for {@link ReactPicker} with {@link Spinner#MODE_DIALOG}.
 */
public class ReactDialogPickerManager extends ReactPickerManager {
    private final ReactNativeHostable mNativeHost;

    private static final String REACT_CLASS = "ZPAndroidDialogPicker";

    public ReactDialogPickerManager(ReactNativeHostable nativeHost) {
        mNativeHost = nativeHost;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactPicker createViewInstance(ThemedReactContext reactContext) {
        try {
            Context context = mNativeHost.getActivityContext() != null ? mNativeHost.getActivityContext() : reactContext;
            return new ReactPicker(context, Spinner.MODE_DIALOG);
        } catch (Exception e) {
            Timber.e(e, "create ReactDialog error");
            return null;
        }

    }
}
