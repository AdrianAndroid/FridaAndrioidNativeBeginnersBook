/*
 * Copyright (C) 2013 AOKP Project
 * Copyright (C) 2013 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ceco.gm2.gravitybox;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;

public class BackgroundAlphaColorDrawable extends ColorDrawable {
    int mBgColor;
    int mAlpha = 255;
    int mComputedDrawColor = 0;

    public BackgroundAlphaColorDrawable(int bgColor) {
        setBgColor(mBgColor = bgColor);
        updateColor();
    }

    public void setBgColor(int color) {
        mBgColor = color;
        updateColor();
    }

    @Override
    public void setColor(int color) {
        mComputedDrawColor = mBgColor = color;
        invalidateSelf();
    }

    @Override
    public int getColor() {
        return mComputedDrawColor;
    }

    @Override
    public void setAlpha(int alpha) {
        if (alpha > 255) {
            alpha = 255;
        } else if (alpha < 0) {
            alpha = 0;
        }
        mAlpha = alpha;
        updateColor();
    }

    public int getBgColor() {
        return mBgColor;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(mComputedDrawColor, Mode.SRC);
    }

    private void updateColor() {
        mComputedDrawColor = applyAlphaToColor(mBgColor, mAlpha);
        invalidateSelf();
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public static int floatAlphaToInt(float alpha) {
        return Math.round(alpha * 255);
    }

    public static int applyAlphaToColor(int color, float alpha) {
        int a = floatAlphaToInt(alpha);
        return applyAlphaToColor(color, a);
    }

    public static int applyAlphaToColor(int color, int alpha) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(alpha, r, g, b);
    }
}
