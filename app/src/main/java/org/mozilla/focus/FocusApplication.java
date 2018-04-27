/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus;

import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.squareup.leakcanary.LeakCanary;

import org.mozilla.focus.download.DownloadInfoManager;
import org.mozilla.focus.history.BrowsingHistoryManager;
import org.mozilla.focus.locale.LocaleAwareApplication;
import org.mozilla.focus.screenshot.ScreenshotManager;
import org.mozilla.focus.search.SearchEngineManager;
import org.mozilla.focus.telemetry.TelemetryWrapper;
import org.mozilla.focus.utils.AdjustHelper;
import org.mozilla.focus.utils.AppConstants;

public class FocusApplication extends LocaleAwareApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        enableStrictMode();

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        SearchEngineManager.getInstance().init(this);

        TelemetryWrapper.init(this);
        AdjustHelper.setupAdjustIfNeeded(this);

        BrowsingHistoryManager.getInstance().init(this);
        ScreenshotManager.getInstance().init(this);
        DownloadInfoManager.getInstance().init(this);

    }

    public static void enableStrictMode() {
        if (AppConstants.isReleaseBuild()) {
            return;
        }

        final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll();
        final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll();

        threadPolicyBuilder.penaltyLog().penaltyDialog();
        // Previously we have penaltyDeath() for debug build, but in order to add crashlytics, we can't use it here.
        // ( crashlytics has untagged Network violation so it always crashes
        vmPolicyBuilder.penaltyLog();

        StrictMode.setThreadPolicy(threadPolicyBuilder.build());
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

}
