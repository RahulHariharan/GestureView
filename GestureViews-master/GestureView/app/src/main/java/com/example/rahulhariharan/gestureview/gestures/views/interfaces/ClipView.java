package com.example.rahulhariharan.gestureview.gestures.views.interfaces;

import android.graphics.RectF;
import android.support.annotation.Nullable;

public interface ClipView {

    /**
     * Clips view so only {@code rect} part (modified by view's state) will be drawn.
     * <p/>
     * Pass {@code null} to turn clipping off.
     */
    void clipView(@Nullable RectF rect, float rotation);

}
