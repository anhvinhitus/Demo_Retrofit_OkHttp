/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.zalopay.apploader.zpmodal;


import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * {@link Event} for dismissing a Dialog.
 */
/* package */ class RequestCloseEvent extends Event<RequestCloseEvent> {

    public static final String EVENT_NAME = "topRequestClose";

    protected RequestCloseEvent(int viewTag) {
        super(viewTag, System.currentTimeMillis());
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), null);
    }
}