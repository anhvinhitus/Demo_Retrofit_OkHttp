/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package vn.com.vng.zalopay.mdl.zpmodal;

import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * {@link Event} for showing a Dialog.
 */
/* package */ class ShowEvent extends Event<ShowEvent> {

  public static final String EVENT_NAME = "topShow";

  protected ShowEvent(int viewTag, long timestampMs) {
    super(viewTag, timestampMs);
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
