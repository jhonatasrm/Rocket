/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import org.mozilla.focus.utils.IntentUtils;
import org.mozilla.focus.utils.SafeIntent;


// This Activity will accept most of the VIEW ACTION and http/https scheme from users' phone
// The telemetry to trace above be behaviour will be placed here
public class LauncherActivity extends Activity {

    private static final String TAG = LauncherActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("aaaa", "onCreate: " + this);

        final SafeIntent safeIntent = new SafeIntent(getIntent());

        // Is this deep link?

        // If it's not a view intent, it won't be a custom tabs intent either. Just launch!
        if (!isViewIntentWithURL(safeIntent)) {

            // Maybe we should ignore here.
        } else {
            dispatchNormalIntent();
        }


        finish();
    }

    /**
     * Launch the browser activity.
     */
    private void dispatchNormalIntent() {
        Intent intent = new Intent(getIntent());
        intent.setClass(this, MainActivity.class);

        filterFlags(intent);

        startActivity(intent);
    }

    private static boolean isViewIntentWithURL(@NonNull final SafeIntent safeIntent) {

        return Intent.ACTION_VIEW.equals(safeIntent.getAction())
                && safeIntent.getDataString() != null;
    }

    private static void filterFlags(Intent intent) {
        // Explicitly remove the new task and clear task flags (Our browser activity is a single
        // task activity and we never want to start a second task here). See bug 1280112.
        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // LauncherActivity is started with the "exclude from recents" flag (set in manifest). We do
        // not want to propagate this flag from the launcher activity to the browser.
        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.getExtras().putBoolean(IntentUtils.EXTRA_FROM_EXTERNAL_APPS, true);
    }


}
