/**
 * Copyright © 2015 by VNG Corporation
 * All rights reserved. No part of this publication may be reproduced, distributed,
 * or transmitted in any form or by any means, including photocopying, recording,
 * or other electronic or mechanical methods, without the prior written permission
 * of the publisher, except in the case of brief quotations embodied in critical reviews
 * and certain other noncommercial uses permitted by copyright law.
 */
package vn.com.zalopay.wallet.utils;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class GuiUtils {
    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                View focus = activity.getCurrentFocus();
                if (focus != null) {
                    inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        }
    }
}
