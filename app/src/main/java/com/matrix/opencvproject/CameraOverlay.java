package com.matrix.opencvproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CameraOverlay extends View {
    private List<Rect> rectStack;
    private Paint defaultPaint;

    public CameraOverlay(Context context, AttributeSet st) {
        super(context, st);
        rectStack = new ArrayList<>();
        defaultPaint = new Paint();
        defaultPaint.setStyle(Paint.Style.STROKE);
        defaultPaint.setStrokeWidth(3);
        defaultPaint.setARGB(255, 255, 0, 255);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Rect rect : rectStack){
            canvas.drawRect(rect, defaultPaint);
        }
    }

    public void setDrawInStack(Rect rect){
        if(rect != null) rectStack.add(rect);
        invalidate();
    }

    public void clearDrawStack(){
        rectStack.clear();
        invalidate();
    }

}
