package com.woo.threedimensionremote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.woo.threedimensionremote.protocol.Sender;

public class SwingDotView extends View {
    private static final String TAG = "SwingDotView";
    private static final float MAX_ACC = 20;
    private float x, z;
    private float mScreenWidth, mScreenHeight;
    private Paint mPointPaint, mTextPaint, mPathPaint;
    private Path mPath;
    private int sensitivity;
    private boolean mShowPath;

    public SwingDotView(Context context) {
        super(context);
        initDraw();
    }

    public SwingDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDraw();
    }

    private void initDraw() {
        mPointPaint = new Paint();
        mPointPaint.setColor(Color.DKGRAY);
        mPointPaint.setStrokeWidth(50); // * density
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.DKGRAY);
        mTextPaint.setTextSize(40);

        mPathPaint = new Paint();
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setColor(Color.GRAY);
        mPathPaint.setStrokeWidth(50); // * density
//        mPathPaint.setStrokeCap(Paint.Cap.BUTT);

        mPath = new Path();
    }

    private void drawPoint(Canvas canvas, float x, float y) {
        canvas.drawPoint(x, y, mPointPaint);

        sendData(x - mScreenWidth / 2, y - mScreenHeight / 2);
    }

    private void sendData(float x, float y) {
        byte[] bx, by;

        bx = MathUtil.int2ByteArray((int) x);
        bx[0] = 0;
        Sender.getInstance().sendData(bx);
        by = MathUtil.int2ByteArray((int) y);
        by[0] = 1;
        Sender.getInstance().sendData(by);
    }

    // change to use angle / need init sensitivity at beginning
    public void setPointPos(float x, float z) {
        x = (float) (Math.round(x * 10)) / 10;
        z = (float) (Math.round(z * 10)) / 10;
        this.x = x * sensitivity + mScreenWidth / 2;
        this.z = z * sensitivity + mScreenHeight / 2;
//        Log.d(TAG, "setPointPos: " + x + "   " + z);
/*        if (this.x + x > mScreenWidth) this.x = mScreenWidth;
        else if(this.x + x < 0) this.x = 0;
        else this.x += x;
        if (this.z + z < mScreenHeight) this.z = mScreenHeight;
        else if(this.z + z < 0) this.z = 0;
        else this.z += z;*/
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity + 1;
    }

    public void setShowPath(boolean showPath) {
        this.mShowPath = showPath;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPoint(canvas, x, z);
        String s = x + "     " + z;
//        Log.d(TAG, "onDraw: "+ mScreenWidth + " " + mScreenHeight);
        canvas.drawText(s, mScreenWidth / 2, mScreenHeight - 50, mTextPaint);

        if (mShowPath) {
            canvas.drawText("PATH", 10, mScreenHeight - 50, mTextPaint);
            canvas.drawPath(mPath, mPathPaint);
            mPath.lineTo(x, z);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mScreenWidth = getMeasuredWidth();
        mScreenHeight = getMeasuredHeight();

        Log.d(TAG, "onMeasure: " + mScreenWidth + " " + mScreenHeight);
        x = mScreenWidth / 2;
        z = mScreenHeight / 2;
        mPath.moveTo(x, z);
    }
}
