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
    private float mScreenWidth, mScreenHeight;
    private float x, y;
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

//        sendData(x - mScreenWidth / 2, y - mScreenHeight / 2);
    }

    public void sendData(float x, float y) {
        byte[] axis;

        axis = MathUtil.packetAxisBytes(MathUtil.int2ByteArray((int) x), MathUtil.int2ByteArray((int) y));
        Sender.getInstance().sendData(axis);
    }

    // change to use angle / need init sensitivity at beginning
    public void setPointPos(float x, float y) {
        x = (float) (Math.round(x * 10)) / 10;
        y = (float) (Math.round(y * 10)) / 10;
        this.x = x + mScreenWidth / 2;
        this.y = y + mScreenHeight / 2;
//        Log.d(TAG, "setPointPos: " + x + "   " + y);
/*        if (this.x + x > mScreenWidth) this.x = mScreenWidth;
        else if(this.x + x < 0) this.x = 0;
        else this.x += x;
        if (this.y + y < mScreenHeight) this.y = mScreenHeight;
        else if(this.y + y < 0) this.y = 0;
        else this.y += y;*/
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
        drawPoint(canvas, x, y);
        String s = x + "     " + y;
//        Log.d(TAG, "onDraw: "+ mScreenWidth + " " + mScreenHeight);
        canvas.drawText(s, mScreenWidth *2 / 3, mScreenHeight - 50, mTextPaint);

        if (mShowPath) {
            canvas.drawText("PATH", 10, mScreenHeight - 50, mTextPaint);
            canvas.drawPath(mPath, mPathPaint);
            mPath.lineTo(x, y);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mScreenWidth = getMeasuredWidth();
        mScreenHeight = getMeasuredHeight();

        Log.d(TAG, "onMeasure: " + mScreenWidth + " " + mScreenHeight);
        x = mScreenWidth / 2;
        y = mScreenHeight / 2;
        mPath.moveTo(x, y);
    }
}
