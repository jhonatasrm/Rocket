package org.mozilla.focus.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by mozilla on 2018/4/16.
 */

public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PackageManager packageManager = getPackageManager();
        //packageManager.setComponentEnabledSetting(new ComponentName(this,"org.mozilla.focus.activity.WelcomeActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

        Intent intent = getIntent();
        intent.setClass(this,MainActivity.class);
        startActivity(intent);

    }
}
