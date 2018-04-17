/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.mozilla.focus.R;
import org.mozilla.focus.locale.LocaleAwareAppCompatActivity;
import org.mozilla.focus.settings.SettingsFragment;
import org.mozilla.focus.widget.DefaultBrowserPreference;

public class SettingsActivity extends LocaleAwareAppCompatActivity {
    public static final int ACTIVITY_RESULT_LOCALE_CHANGED = 1;
    //TODO: naming should change
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();

        // Ensure all locale specific Strings are initialised on first run, we don't set the title
        // anywhere before now (the title can only be set via AndroidManifest, and ensuring
        // that that loads the correct locale string is tricky).
        applyLocale();
    }

    @Override
    public void applyLocale() {
        setTitle(R.string.menu_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("nevin", "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");
                final String component = "org.mozilla.focus.activity.WelcomeActivity";

                // TODO: check component here, we only care about Welcome/Fake activity
                // TODO: Maybe show a dialog here cause we don't want the activity to unregister this reciever.
                // TODO: Or maybe we can move it somewhere else.... or even just don't care.
                // we are receiving a disable change event
                if (DefaultBrowserPreference.isComponentEnabled(context.getPackageManager(), context.getPackageName(), component)) {
                    // now it's time to let the user choose again
                    DefaultBrowserPreference.makeUsDefaultByOpenAnUrl(SettingsActivity.this);
                } else {
                    // we are receiving an enable change event, now disable it
                    context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, component), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
                }


                // for debug

//                if (intent != null) {
//                    if (intent.getExtras() != null) {
//                        for (String s : intent.getExtras().keySet()) {
//                            Log.d("nevin", s+"===="+intent.getExtras().get(s));
//                        }
//                    }
//                }
            }
        };

        // we need below filter to listen to fake activity enable event
        final IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);


        filter.addDataScheme("package");

        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
