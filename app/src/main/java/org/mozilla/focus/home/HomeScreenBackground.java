/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;

import org.mozilla.focus.R;

public class HomeScreenBackground extends View {

    private Paint paint;
    private static int colors[] = {
            Color.argb(0xff, 0xff, 0xff, 0xff),
            Color.argb(0x88, 0xff, 0xff, 0xff),
            Color.argb(0x55, 0xff, 0xff, 0xff),
            Color.argb(0x00, 0xff, 0xff, 0xff),
    };
    float positions[] = {
            0.0f,
            0.4f,
            0.7f,
            1f
    };

    private static int[][] themeColor = new int[][]{
            new int[]{
                    0xFF41B7E6,
                    0xFF95E2BD
            },
            new int[]{
                    0xFFB7E641,
                    0xFFE2BD95
            },
            new int[]{
                    0xFFE6B741,
                    0xFFBDE295
            },
            new int[]{
                    0xFF41E6B7,
                    0xFF95BDE2
            },
            new int[]{
                    0xFFE641B7,
                    0xFFBD95E2
            },
            new int[]{
                    0xFFB741E6,
                    0xFFE295BD
            }
    };

    private static int themeId = 0;


    public HomeScreenBackground(Context context) {
        this(context, null);
    }

    public HomeScreenBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        switchBackground();
        init();
    }

    void init() {
        Rect rect = new Rect();
        ((Activity) getContext()).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home_pattern);
        paint = new Paint();
        Shader shader1 = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        Shader shader2 = new LinearGradient(0, rect.top, 0, rect.bottom, colors, positions, Shader.TileMode.CLAMP);
        paint.setShader(new ComposeShader(shader2, shader1, PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    protected void resetBackground() {
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.BL_TR, themeColor[0]);
        setBackground(gradient);
    }

    protected void switchBackground() {
        final int[] nextThemeColor = themeColor[themeId % themeColor.length];
        themeId++;
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.BL_TR, nextThemeColor);
        setBackground(gradient);
    }

}
