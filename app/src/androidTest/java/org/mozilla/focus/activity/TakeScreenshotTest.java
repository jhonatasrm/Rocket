/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.Keep;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.focus.R;
import org.mozilla.focus.helper.ScreenshotIdlingResource;
import org.mozilla.focus.helper.SessionLoadedIdlingResource;
import org.mozilla.focus.utils.AndroidTestUtils;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

@Keep
@RunWith(AndroidJUnit4.class)
public class TakeScreenshotTest {

    private static final String TEST_PATH = "/";
    private static final String HTML_FILE_GET_LOCATION = "get_location.html";

    private SessionLoadedIdlingResource sessionLoadedIdlingResource;
    private ScreenshotIdlingResource screenshotIdlingResource;
    private MockWebServer webServer;

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, false) {
        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();

            webServer = new MockWebServer();
            try {
                webServer.enqueue(new MockResponse()
                        .setBody(AndroidTestUtils.readTestAsset(HTML_FILE_GET_LOCATION))
                        .addHeader("Set-Cookie", "sphere=battery; Expires=Wed, 21 Oct 2035 07:28:00 GMT;"));
                webServer.start();
            } catch (IOException e) {
                throw new AssertionError("Could not start web server", e);
            }
        }

        @Override
        protected void afterActivityFinished() {
            super.afterActivityFinished();

            try {
                webServer.close();
                webServer.shutdown();
            } catch (IOException e) {
                throw new AssertionError("Could not stop web server", e);
            }
        }
    };

    @Rule
    public final GrantPermissionRule writePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Rule
    public final GrantPermissionRule readPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);

    @Before
    public void setUp() {
        AndroidTestUtils.beforeTest();
    }

    @After
    public void tearDown() {
        if (sessionLoadedIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(sessionLoadedIdlingResource);
        }
        if (screenshotIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(screenshotIdlingResource);
        }
        if (activityTestRule.getActivity() != null) {
            activityTestRule.getActivity().finishAndRemoveTask();
        }
    }

    @Test
    public void takeScreenshot_screenshotIsCaptured () {

        activityTestRule.launchActivity(new Intent());

        sessionLoadedIdlingResource = new SessionLoadedIdlingResource(activityTestRule.getActivity());

        // Click search field
        onView(allOf(withId(R.id.home_fragment_fake_input), isDisplayed())).perform(click());

        // Enter test site url
        onView(allOf(withId(R.id.url_edit), isDisplayed())).perform(replaceText(webServer.url(TEST_PATH).toString()), pressImeActionButton());

        // Check if test site is loaded
        IdlingRegistry.getInstance().register(sessionLoadedIdlingResource);
        onView(allOf(withId(R.id.display_url), isDisplayed())).check(matches(withText(webServer.url(TEST_PATH).toString())));
        IdlingRegistry.getInstance().unregister(sessionLoadedIdlingResource);

        screenshotIdlingResource = new ScreenshotIdlingResource(activityTestRule.getActivity());
        screenshotIdlingResource.registerScreenshotObserver();

        // Click screen capture button
        onView(allOf(withId(R.id.btn_capture), isDisplayed())).perform(click());

        // Register screenshot taken idling resource and wait capture complete
        IdlingRegistry.getInstance().register(screenshotIdlingResource);

        IdlingRegistry.getInstance().unregister(screenshotIdlingResource);

        // Open menu
        onView(allOf(withId(R.id.btn_menu), isDisplayed())).perform(click());

        // Click my shot
        onView(allOf(withId(R.id.menu_screenshots), isDisplayed())).perform(click());

        // Click the first item in my shots panel
        // Since "index=0" in ScreenshotItemAdapter is always date label, the first screenshot item will start from "index=1".
        onView(withId(R.id.screenshot_grid_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition(1, click()));

        // Check if screenshot is displayed
        onView(withId(R.id.screenshot_viewer_image)).check(matches(isDisplayed()));

        // Check if open url/edit/share/info/delete button is there
        onView(withId(R.id.screenshot_viewer_btn_open_url)).check(matches(isDisplayed()));
        onView(withId(R.id.screenshot_viewer_btn_edit)).check(matches(isDisplayed()));
        onView(withId(R.id.screenshot_viewer_btn_share)).check(matches(isDisplayed()));
        onView(withId(R.id.screenshot_viewer_btn_info)).check(matches(isDisplayed()));
        onView(withId(R.id.screenshot_viewer_btn_delete)).check(matches(isDisplayed()));

        // Delete the screenshot
        onView(withId(R.id.screenshot_viewer_btn_delete)).perform(click());

        // Confirm delete
        onView(allOf(withText(R.string.browsing_history_menu_delete), isDisplayed())).perform(click());

        // Check if come back to my shots panel
        onView(withId(R.id.screenshots)).check(matches(isDisplayed()));

        // Back to home
        Espresso.pressBack();
    }
}