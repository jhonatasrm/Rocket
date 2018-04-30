/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.example.firebase.BuildConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * It's a wrapper to communicate with Firebase
 */
abstract class FirebaseWrapper {

    private static final String TAG = "FirebaseWrapper";

    // Instance of FirebaseWrapper that provides default values
    static FirebaseWrapper instance;


    // get Remote Config string
    static String getRcString(@NonNull Context context, @NonNull String key) {
        final Object value = instance.getRemoteConfigDefault(context).get(key);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }


    @WorkerThread
    static void updateInstanceId(boolean enable) {
    }

    static void enableCloudMessaging(Context context, String componentName, boolean enable) {


    }

    static void initCrashlytics() {


    }

    // Replace DefaultUncaughtExceptionHandler with our naive implementation
    // We don't need to cache the original UncaughtExceptionHandler
    // If we want to restart crashlytics, we just restart the app and don't call this method here.
    static void enableCrashlytics(boolean enable) {

    }

    static void enableAnalytics(Context context, boolean enable) {
    }

    // This need to be run in worker thread since FirebaseRemoteConfigSettings has IO access
    static void enableRemoteConfig(Context context, boolean enable) {
    }

    // Client code must implement this method so it's not static here.
    abstract HashMap<String, Object> getRemoteConfigDefault(Context context);
}