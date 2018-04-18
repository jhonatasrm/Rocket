/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.widget;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;

import org.mozilla.focus.R;
import org.mozilla.focus.activity.InfoActivity;
import org.mozilla.focus.utils.Browsers;
import org.mozilla.focus.utils.IntentUtils;
import org.mozilla.focus.utils.Settings;
import org.mozilla.focus.utils.SupportUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@TargetApi(Build.VERSION_CODES.N)
public class DefaultBrowserPreference extends Preference {
    private Switch switchView;

    @SuppressWarnings("unused") // Instantiated from XML
    public DefaultBrowserPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.preference_default_browser);
    }

    @SuppressWarnings("unused") // Instantiated from XML
    public DefaultBrowserPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_default_browser);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        switchView = (Switch) view.findViewById(R.id.switch_widget);
        update();
    }

    public void update() {
        if (switchView != null) {
            final boolean isDefaultBrowser = Browsers.isDefaultBrowser(getContext());
            switchView.setChecked(isDefaultBrowser);
            Settings.updatePrefDefaultBrowserIfNeeded(getContext(), isDefaultBrowser);
        }
    }

    @Override
    protected void onClick() {
        final Context context = getContext();
        final String component = "org.mozilla.focus.activity.WelcomeActivity";



//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            openDefaultAppsSettings(context);
//            return;
//        }
//        // maybe remove this
//        openSumoPage(context);



// method 1 ====== just like UC Browser =====
// this will open the app setting for current default browser, so you can revoke it.
// the second time you click it, it'll show a chooser to let users make as default
// TODO: Show a dialog first to give users more context what to do next (e.g. revoke default, and set again)
//        Browsers browsers = new Browsers(getContext(), "https://mozilla.org");
//        if (Browsers.isDefaultBrowser(context)) {
//            if (browsers.getDefaultBrowser() != null) {
//                // let the user to revoke the original default browser
//                final Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                i.addCategory(Intent.CATEGORY_DEFAULT);
//                i.setData(Uri.parse("package:com.UCMobile.intl"));
//                context.startActivity(i);
//            } else {
//
//                // now it's time to set ourselves default
//                makeUsDefaultByOpenAnUrl(context);
//            }
//
//        }

// method 2 ====== This is wired. There'll be more than one default browser if you
// first choose A browser then choose B browser =====
        // this must be a http since we use http scheme to determine default browser
        final String defaultBrowserUrl = "http://www.mozilla.org";
        final PackageManager p = context.getPackageManager();
        final ComponentName cN = new ComponentName(context, component);
        p.setComponentEnabledSetting(cN,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        final Intent selector = new Intent(Intent.ACTION_VIEW);
        selector.setData(Uri.parse(defaultBrowserUrl));
        context.startActivity(selector);
        p.setComponentEnabledSetting(cN,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);


    }

    private void openDefaultAppsSettings(Context context) {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // In some cases, a matching Activity may not exist (according to the Android docs).
            openSumoPage(context);
        }
    }

    private void openSumoPage(Context context) {
        final Intent intent = InfoActivity.getIntentFor(context, SupportUtils.getSumoURLForTopic(context, "rocket-default"), getTitle().toString());
        context.startActivity(intent);
    }

}
