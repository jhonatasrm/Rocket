/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;

import org.mozilla.focus.persistence.TabsDatabase;
import org.mozilla.focus.utils.AppConstants;

public class Inject {

    private static final String TOP_SITES = "[{\"id\":-1,\"url\":\"file:\\/\\/\\/android_asset\\/gpl.html\",\"title\":\"Sample Top Site\",\"favicon\":\"ic_youtube.png\",\"viewCount\":20,\"lastViewTimestamp\":1517196818119},{\"id\":-3,\"url\":\"http:\\/\\/m.tribunnews.com\\/\",\"title\":\"Tribunnews\",\"favicon\":\"ic_tribunnews.png\",\"viewCount\":18,\"lastViewTimestamp\":1517196818119},{\"id\":-5,\"url\":\"https:\\/\\/m.tokopedia.com\\/\",\"title\":\"Tokopedia\",\"favicon\":\"ic_tokopedia.png\",\"viewCount\":16,\"lastViewTimestamp\":1517196818119},{\"id\":-4,\"url\":\"https:\\/\\/m.facebook.com\\/\",\"title\":\"Facebook\",\"favicon\":\"ic_facebook.png\",\"viewCount\":14,\"lastViewTimestamp\":1517196818119},{\"id\":-8,\"url\":\"https:\\/\\/m.bukalapak.com\\/\",\"title\":\"Bukalapak\",\"favicon\":\"ic_bukalapak.png\",\"viewCount\":12,\"lastViewTimestamp\":1517196818119},{\"id\":-6,\"url\":\"http:\\/\\/m.liputan6.com\\/\",\"title\":\"Liputan6\",\"favicon\":\"ic_liputan6.png\",\"viewCount\":10,\"lastViewTimestamp\":1517196818119},{\"id\":-7,\"url\":\"http:\\/\\/www.kompas.com\\/\",\"title\":\"Kompas\",\"favicon\":\"ic_kompas.png\",\"viewCount\":8,\"lastViewTimestamp\":1517196818119},{\"id\":-9,\"url\":\"https:\\/\\/m.kapanlagi.com\\/\",\"title\":\"Kapanlagi\",\"favicon\":\"ic_kapanlagi.png\",\"viewCount\":6,\"lastViewTimestamp\":1517196818119}]";

    private static volatile TabsDatabase tabsDatabase;

    public static String getDefaultTopSites(Context context) {
        return TOP_SITES;
    }

    public static TabsDatabase getTabsDatabase(Context context) {
        if (tabsDatabase == null || !tabsDatabase.isOpen()) {
            synchronized (Inject.class) {
                if (tabsDatabase == null || !tabsDatabase.isOpen()) {
                    // using an in-memory database because the information stored here disappears
                    // when the process is killed
                    tabsDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), TabsDatabase.class)
                            .build();
                }
            }
        }
        return tabsDatabase;
    }


    // The pref is not persist so we need to inject the condition instead of override defaultSharedPreference
    public static boolean isTelemetryEnabled(Context context) {
        // The first access to shared preferences will require a disk read.
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.allowThreadDiskReads();
        try {
            final Resources resources = context.getResources();
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            return preferences.getBoolean(resources.getString(R.string.pref_key_telemetry), true);
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }

    }

}
