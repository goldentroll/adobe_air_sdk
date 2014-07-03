/**
 * AdjustIoSDKInitializer.java
 * AdjustIoSDKInitializer
 *
 * Created by Andrew Slotin on 2013-11-20.
 * Copyright (c) 2012-2014 adjust GmbH.  All rights reserved.
 * See the file MIT-LICENSE for copying permission.
 */

package com.adeven.adjustio;

import android.app.Activity;

public class AdjustIoSDKInitializer {
    public static void initialize(Activity activity, String appToken, String environment, Boolean eventBufferingEnabled) {
        AdjustIo.appDidLaunch(activity, appToken, environment, eventBufferingEnabled);
        AdjustIo.setSdkPrefix("air2.2.1");
        AdjustIo.onResume(activity);
    }
}
