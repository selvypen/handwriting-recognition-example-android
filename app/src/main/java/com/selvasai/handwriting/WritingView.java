/*!
 *  @date 2020/01/02
 *  @file WritingView.java
 *  @author SELVAS AI
 *
 *  Copyright 2020. SELVAS AI Inc. All Rights Reserved.
 */

package com.selvasai.handwriting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class WritingView extends View {

    private WritingRecognizer mRecognizer = null;
    private Paint mPaint = null;
    private Bitmap mBitmap = null;
    private Canvas mCanvas = null;
    private Path mPath = null;
    private float mX = 0;
    private float mY = 0;
    private int mWidth = 0;
    private int mHeight = 0;

    public WritingView(Context context) {
        super(context);
    }

    public WritingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initialize(int width, int height) {
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        if (mCanvas == null) {
            mCanvas = new Canvas(mBitmap);
        }
        if (mPath == null) {
            mPath = new Path();
        }
        if (mPaint == null) {
            mPaint = new Paint();
        }

        clear();
    }

    private void initPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        mPaint.setColor(getResources().getColor(R.color.colorStroke));
    }

    private void drawGuideText() {
        mPaint.setColor(getResources().getColor(R.color.colorGuideText));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(3);
        mPaint.setTextSize(20.0f * getResources().getDisplayMetrics().scaledDensity);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mCanvas.drawText(getResources().getString(R.string.writeHere), mWidth / 2, mHeight / 2, mPaint);
    }

    public void setRecognizer(WritingRecognizer recognizer) {
        mRecognizer = recognizer;
    }

    public void clear() {
        mPath.reset();
        mCanvas.drawColor(getResources().getColor(R.color.colorCanvas));
        drawGuideText();
        initPaint();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        View v = findViewById(R.id.canvas);
        mWidth = v.getWidth();
        mHeight = v.getHeight();
        initialize(mWidth, mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                handleTouchUp();
                break;
        }
        return true;
    }

    private void handleTouchDown(float x, float y) {
        if (mRecognizer != null) {
            mRecognizer.addPoint((int) x, (int) y);
        }

        mPath.reset();
        mPath.moveTo(x, y);
        mCanvas.drawCircle(x, y, 1, mPaint);
        mX = x;
        mY = y;
    }

    private void handleTouchMove(float x, float y) {
        if (mRecognizer != null) {
            mRecognizer.addPoint((int) x, (int) y);
        }

        mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        mX = x;
        mY = y;
        mCanvas.drawPath(mPath, mPaint);
    }

    private void handleTouchUp() {
        if (mRecognizer != null) {
            mRecognizer.endStroke();
        }

        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
    }
}
