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



//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            openDefaultAppsSettings(context);
//            return;
//        }
//        // maybe remove this
//        openSumoPage(context);



// method 1 ====== just like UC Browser =====
// this will open the app setting for current default browser, so you can revoke it.
// the second time you click it, it'll show a chooser to let users make as default
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
//
//        final String component = "org.mozilla.focus.activity.WelcomeActivity";
//        final boolean componentEnabled = isComponentEnabled(context.getPackageManager(), context.getPackageName(), component);
//        if (componentEnabled) {
//            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, component), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
//
//        } else {
//            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, component), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//
//        }

// method 2 ====== This is wired. There'll be more than one default browser if you
// first choose A browser then choose B browser =====
//        PackageManager p = context.getPackageManager();
//        ComponentName cN = new ComponentName(c, component);
//        p.setComponentEnabledSetting(cN,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);
//
//        Intent selector = new Intent(Intent.ACTION_VIEW);
//        selector.addCategory(Intent.CATEGORY_BROWSABLE);
//        selector.addCategory(Intent.CATEGORY_DEFAULT);
//        selector.setData(Uri.parse("https://google.com"));
//        context.startActivity(selector);
//        p.setComponentEnabledSetting(cN,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);

        // method 3 ==== I'd prefer this one
        final String component = "org.mozilla.focus.activity.WelcomeActivity";
        final PackageManager packageManager = context.getPackageManager();

        // if the component is enabled, disable it.
        if (isComponentEnabled(packageManager, context.getPackageName(), component)) {
            // this should never happen
            // context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, component), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
        } else {
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, component), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            // we'll wait for setting activity's BroadcastReceiver to try set as default again.
        }

    }

    public static void makeUsDefaultByOpenAnUrl(Context context) {
        final String sumo = SupportUtils.getSumoURLForTopic(context, "rocket-default");
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(sumo));
        context.startActivity(i);
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

    public static boolean isComponentEnabled(PackageManager pm, String pkgName, String clsName) {
        ComponentName componentName = new ComponentName(pkgName, clsName);
        int componentEnabledSetting = pm.getComponentEnabledSetting(componentName);

        switch (componentEnabledSetting) {
            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                return false;
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                return true;
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
            default:
                // We need to get the application info to get the component's default state
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES
                            | PackageManager.GET_RECEIVERS
                            | PackageManager.GET_SERVICES
                            | PackageManager.GET_PROVIDERS
                            | PackageManager.GET_DISABLED_COMPONENTS);

                    List<ComponentInfo> components = new ArrayList<>();
                    if (packageInfo.activities != null) Collections.addAll(components, packageInfo.activities);
                    if (packageInfo.services != null) Collections.addAll(components, packageInfo.services);
                    if (packageInfo.providers != null) Collections.addAll(components, packageInfo.providers);

                    for (ComponentInfo componentInfo : components) {
                        if (componentInfo.name.equals(clsName)) {
                            return componentInfo.isEnabled();
                        }
                    }

                    // the component is not declared in the AndroidManifest
                    return false;
                } catch (PackageManager.NameNotFoundException e) {
                    // the package isn't installed on the device
                    return false;
                }
        }
    }

}
