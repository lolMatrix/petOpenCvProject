package com.matrix.opencvproject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class PreviewGroup extends ViewGroup {

    private CameraOverlay overlay;
    private PreviewView preview;

    public PreviewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
