package com.woo.threedimensionremote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SwingDotView extends View {
    private static final String TAG = "SwingDotView";
    private Paint mPointPaint;
    private float x, z;
    private float mScreenWidth, mScreenHeight;
    private static final float MAX_ACC = 20;

    public SwingDotView(Context context) {
        super(context);
        initDraw();
    }

    public SwingDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDraw();
    }

    void initDraw() {
        mPointPaint = new Paint();
        mPointPaint.setColor(Color.DKGRAY);
        mPointPaint.setStrokeWidth(50); // * density
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    void drawPoint(Canvas canvas, float x, float y) {
        canvas.drawPoint(x, y, mPointPaint);
    }

    // change to use angle / need init tv screen size & distance at beginning
    void setPointPos(float x, float y) {
        if (this.x + x > mScreenWidth) this.x = mScreenWidth;
        else if(this.x + x < 0) this.x = 0;
        else this.x += x;
        if (this.z + y < mScreenHeight) this.z = mScreenHeight;
        else if(this.z + y < 0) this.z = 0;
        else this.z += y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPoint(canvas, x, z);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mScreenWidth = getWidth();
        mScreenHeight = getHeight();

        x = mScreenWidth / 2;
        z = mScreenHeight / 2;
    }
}
