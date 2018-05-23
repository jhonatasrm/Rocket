package org.mozilla.focus.screenshot;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import org.mozilla.focus.R;
import org.mozilla.focus.fragment.BrowserFragment;
import org.mozilla.focus.fragment.ScreenCaptureDialogFragment;
import org.mozilla.focus.screenshot.model.Screenshot;
import org.mozilla.focus.telemetry.TelemetryWrapper;
import org.mozilla.focus.utils.FirebaseHelper;
import org.mozilla.focus.utils.ThreadUtils;

import java.lang.ref.WeakReference;

import static org.mozilla.focus.utils.FirebaseHelper.AFTER_ACTION_SCREENSHOT;
import static org.mozilla.focus.utils.FirebaseHelper.AFTER_ACTION_SCREENSHOT_EDIT;
import static org.mozilla.focus.utils.FirebaseHelper.AFTER_ACTION_SCREENSHOT_OPEN;
import static org.mozilla.focus.utils.FirebaseHelper.AFTER_ACTION_SCREENSHOT_SHARE;

public class CaptureRunnable extends ScreenshotCaptureTask implements Runnable, BrowserFragment.ScreenshotCallback {

    final WeakReference<Context> refContext;
    final WeakReference<BrowserFragment> refBrowserFragment;
    final WeakReference<ScreenCaptureDialogFragment> refScreenCaptureDialogFragment;
    final WeakReference<View> refContainerView;
    private static volatile boolean completed;

    public CaptureRunnable(Context context, BrowserFragment browserFragment, ScreenCaptureDialogFragment screenCaptureDialogFragment, View container) {
        super(context);
        refContext = new WeakReference<>(context);
        refBrowserFragment = new WeakReference<>(browserFragment);
        refScreenCaptureDialogFragment = new WeakReference<>(screenCaptureDialogFragment);
        refContainerView = new WeakReference<>(container);
        setCompleted(false);
    }

    public static boolean isCompleted() {
        return completed;
    }

    public static void setCompleted(boolean completed) {
        CaptureRunnable.completed = completed;
    }

    @Override
    public void run() {
        BrowserFragment browserFragment = refBrowserFragment.get();
        if (browserFragment == null) {
            return;
        }
        if (browserFragment.capturePage(this)) {
            //  onCaptureComplete called
        } else {
            //  Capture failed
            ScreenCaptureDialogFragment screenCaptureDialogFragment = refScreenCaptureDialogFragment.get();
            if (screenCaptureDialogFragment != null) {
                screenCaptureDialogFragment.dismiss();
            }
            promptScreenshotResult(R.string.screenshot_failed, null);
        }
    }

    @Override
    public void onCaptureComplete(String title, String url, Bitmap bitmap) {
        Context context = refContext.get();
        if (context == null) {
            return;
        }

        execute(title, url, bitmap);
    }

    @Override
    protected void onPostExecute(final Screenshot screenshot) {
        ScreenCaptureDialogFragment screenCaptureDialogFragment = refScreenCaptureDialogFragment.get();
        if (screenCaptureDialogFragment == null) {
            cancel(true);
            return;
        }
        final int captureResultResource = screenshot == null ? R.string.screenshot_failed : R.string.screenshot_saved;
        screenCaptureDialogFragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                promptScreenshotResult(captureResultResource, screenshot);
            }
        });
        if (null == screenshot) {
            screenCaptureDialogFragment.dismiss();
        } else {
            screenCaptureDialogFragment.dismiss(true);
        }
    }

    private void promptScreenshotResult(int snackbarTitleId, @NonNull Screenshot screenshot) {
        Context context = refContext.get();
        if (context == null) {
            return;
        }
        // TODO: request permission here
        if (context instanceof Activity) {
            final Activity activity = ((Activity) context);
            final long after_action_screenshot = FirebaseHelper.getRcLong(context, AFTER_ACTION_SCREENSHOT);
            if (after_action_screenshot == AFTER_ACTION_SCREENSHOT_SHARE) {
                showSnackbar(activity, snackbarTitleId, R.string.after_action_screenshot_share, v -> {
                    onShareClick(activity, screenshot);
                });
            } else if (after_action_screenshot == AFTER_ACTION_SCREENSHOT_EDIT) {
                showSnackbar(activity, snackbarTitleId, R.string.after_action_screenshot_edit, v -> {
                    onEditClick(activity, screenshot);
                });
            } else if (after_action_screenshot == AFTER_ACTION_SCREENSHOT_OPEN) {
                showSnackbar(activity, snackbarTitleId, R.string.after_action_screenshot_open, v -> {
                    onOpenClick(activity, screenshot);

                });
            } else {
                defaultMessage(snackbarTitleId, context);

            }
        } else {
            defaultMessage(snackbarTitleId, context);
        }

        completed = true;
    }

    @NonNull
    private void showSnackbar(Activity activity, int snackbarTitleId, int actStrRes, View.OnClickListener listener) {
        final Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.browser_container),
                snackbarTitleId, Snackbar.LENGTH_SHORT);
        snackbar.setAction(actStrRes, listener);
        snackbar.show();
    }

    private void defaultMessage(int snackbarTitleId, Context context) {
        Toast.makeText(context, snackbarTitleId, Toast.LENGTH_SHORT).show();
    }


    private void onEditClick(Activity activity, Screenshot screenshot) {
        ThreadUtils.postToBackgroundThread(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse(screenshot.getImageUri());//Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                Intent editIntent = new Intent(Intent.ACTION_EDIT);
                editIntent.setDataAndType(uri, "image/*");
                editIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    activity.startActivity(Intent.createChooser(editIntent, null));
                    TelemetryWrapper.editCaptureImage(activity, true);
                } catch (ActivityNotFoundException e) {
                    TelemetryWrapper.editCaptureImage(activity, false);
                }
            }
        });
    }

    private void onShareClick(Activity activity, Screenshot screenshot) {
        ThreadUtils.postToBackgroundThread(new Runnable() {
            @Override
            public void run() {
                Uri uri = Uri.parse(screenshot.getImageUri());
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.setType("image/*");
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    activity.startActivity(Intent.createChooser(share, null));
                    TelemetryWrapper.shareCaptureImage(activity, false);
                } catch (ActivityNotFoundException e) {
                }
            }
        });
    }

    private void onOpenClick(Activity activity, Screenshot screenshot) {
        ScreenshotViewerActivity.goScreenshotViewerActivityOnResult(activity, screenshot);
        TelemetryWrapper.openCapture();
    }


}
