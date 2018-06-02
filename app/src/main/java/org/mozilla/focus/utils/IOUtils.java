/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.utils;

import android.content.Context;
import android.net.http.HttpResponseCache;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class IOUtils {

    private static final String TAG = "IOUtils";
    private static final String HTTP_CACHE_DIR = "http_cache";

    public static JSONObject readAsset(Context context, String fileName) throws IOException {
        return read(context.getAssets().open(fileName), "Corrupt JSON asset (" + fileName + ")");
    }

    public static JSONObject readUrl(URL url) throws IOException {
        return read(url.openStream(), "Can't read from URL: (" + url.toString() + ")");
    }

    private static JSONObject read(InputStream open, String errorMessage) throws IOException {

        try (final BufferedReader reader =
                     new BufferedReader(new InputStreamReader(open, StandardCharsets.UTF_8))) {
            final StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return new JSONObject(builder.toString());
        } catch (JSONException e) {
            throw new AssertionError(errorMessage, e);
        }
    }
    public static void initHttpCacheDir(Context context) {
        ThreadUtils.postToBackgroundThread(() -> {
            try {
                File httpCacheDir = new File(context.getCacheDir(), HTTP_CACHE_DIR);
                if (httpCacheDir.exists()) return;
                long size = 10 * 1024 * 1024; // 10MB
                HttpResponseCache.install(httpCacheDir, size);
            } catch (IOException e) {
                Log.e(TAG, "initHttpCacheDir failed: ", e);
            }
        });
    }
    @WorkerThread
    static void clearHttpCacheDir() {
        final HttpResponseCache installed = HttpResponseCache.getInstalled();
        if (installed != null) {
            installed.flush();
        }
    }
}
