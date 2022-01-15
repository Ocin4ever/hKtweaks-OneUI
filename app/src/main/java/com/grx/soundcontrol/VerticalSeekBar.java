/*
 *    Grouxho (Grx)
 *
 *   This file is based  in the class VerticalSeekBar.java from Haruki Hasegawa  - https://github.com/h6ah4i/android-verticalseekbar
 *
 *   - Features added by Grx:
 *
 *           - Zero Offset
 *           - Progress line drawn from zero offset
 *           - Dividers
 *           - Show or hide Background track
 *
 *
 *
 *     To do so, some code from aosp sources has been taken from aosp frameworks
 *
 *
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 * This file contains AOSP code copied from /frameworks/base/core/java/android/widget/AbsSeekBar.java
 *
 *
 * Copyright (C) 2007 The Android Open Source Project
 *
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
 *
 */

package com.grx.soundcontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.hades.hKtweaks.utils.kernel.sound.ArizonaSound;

import de.dlyt.yanndroid.oneui.view.SeekBar;

public class VerticalSeekBar extends SeekBar {
    public boolean mGrxIsInit = false;
    public int mGrxZeroOffset = 12;

    public VerticalSeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setMode(3);
    }

    public void grxSetInitialized(boolean initialized) {
        mGrxIsInit = initialized;
    }

    public void grxSetSeekBarProgress(String value) {
        if (value == null || value.isEmpty()) setProgress(mGrxZeroOffset);
        else setProgress(Integer.valueOf(value) + mGrxZeroOffset);
    }

    public void grxSetCurrentKernelValue(int band) {
        grxSetSeekBarProgress(ArizonaSound.getEqValues().get(band));
    }

    public int grxGetNormalizedProgress() {
        if (mGrxIsInit) return getProgress() - mGrxZeroOffset;
        else return 0;
    }

    public String grxGetNormalizedStringProgress() {
        if (mGrxIsInit) return String.valueOf(getProgress() - mGrxZeroOffset);
        else return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return true;

        boolean handled = super.onTouchEvent(event);
        if (handled) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    attemptClaimDrag();
                    break;
            }
        }

        return handled;
    }

    @Override
    public void drawTickMarks(Canvas var1) {
        if (this.mTickMark != null) {
            int var2 = this.getMax() - this.getMin();
            int var3 = 1;
            if (var2 > 1) {
                int var4 = this.mTickMark.getIntrinsicWidth();
                int var5 = this.mTickMark.getIntrinsicHeight();
                if (var4 >= 0) {
                    var4 /= 2;
                } else {
                    var4 = 1;
                }

                if (var5 >= 0) {
                    var3 = var5 / 2;
                }

                this.mTickMark.setBounds(-var4, -var3, var4, var3);
                float var6 = (float) (this.getHeight() - this.getPaddingTop() - this.getPaddingBottom()) / (float) var2;
                var3 = var1.save();
                var1.translate((float) this.getWidth() / 2.0F, (float) this.getPaddingTop());

                for (var4 = 0; var4 <= var2; var4 += 2) {
                    this.mTickMark.draw(var1);
                    var1.translate(0.0F, 2 * var6);
                }

                var1.restoreToCount(var3);
            }
        }

    }
}
