/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.rocket.tabs;

import android.support.annotation.NonNull;

/**
 * An abstract layer of @see{android.webkit.WebViewClient}
 */
public class TabViewClient {

    public void onPageStarted(String url) {
    }

    public void onPageFinished(boolean isSecure) {
    }

    public void onURLChanged(String url) {
    }

    /**
     * Return true if the URL was handled, false if we should continue loading the current URL.
     */
    public boolean handleExternalUrl(String url) {
        return false;
    }


    public void updateFailingUrl(String url, boolean updateFromError) {
    }

    public void onHttpAuthRequest(@NonNull HttpAuthCallback callback, String host, String realm) {
    }

    public interface HttpAuthCallback {
        void proceed(String username, String password);

        void cancel();
    }
}
