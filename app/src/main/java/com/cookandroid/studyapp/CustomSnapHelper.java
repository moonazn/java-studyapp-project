package com.cookandroid.studyapp;

import android.util.DisplayMetrics;
import android.view.View;

import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CustomSnapHelper extends LinearSnapHelper {

    private static final float MILLISECONDS_PER_INCH = 100f;

    public float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
    }

    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        int targetSnapPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        if (velocityX != 0 || velocityY != 0) {
            View snapView = findSnapView(layoutManager);
            if (snapView != null) {
                int[] snapDistance = calculateDistanceToFinalSnap(layoutManager, snapView);
                if (snapDistance[0] == 0 && snapDistance[1] == 0) {
                    return RecyclerView.NO_POSITION;
                }
            }
        }
        return targetSnapPosition;
    }
}
