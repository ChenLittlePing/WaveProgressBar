package com.waveprogress;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * A loading view with wave animation
 *
 * @author ChenLittlePing (562818444@qq.com)
 * @version v1.0
 * @Datetime 2017-05-10 15:00
 */

public class WaveProgress extends View {

    /**The wave path to draw*/
    private Path mWavePath;

    /**The round shape path*/
    private Path mCirclePath;

    /**The paint to draw the wave*/
    private Paint mWavePaint;

    /**The paint to stroke the round canvas*/
    private Paint mCircleStrokePaint;

    /**The paint to fill the round canvas*/
    private Paint mCircleFillPaint;

    /**The wave length*/
    private int mOneWaveLength = 1000;

    /**The wave height*/
    private int mOneWaveHeight = 100;

    /**The wave's scrolling distance in X direction*/
    private int mAimDx = 0;

    /**The wave's scrolling distance in Y direction*/
    private int mAnimDY = 0;

    /**Wave animator*/
    private ValueAnimator mWaveAnimator;

    /**Progress animator*/
    private ValueAnimator mProgressAnimator;

    private int mWaveColor = Color.GREEN;
    private int mFillColor = Color.WHITE;
    private int mStrokeColor = Color.GREEN;
    private int mTextColor = Color.WHITE;

    public WaveProgress(Context context) {
        super(context);
        init();
    }

    public WaveProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        init();
    }

    public WaveProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context, attrs);
        init();
    }

    private void init() {
        mWavePath = new Path();
        mCirclePath = new Path();

        mWavePaint = new Paint();
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mWavePaint.setAntiAlias(true);

        mCircleStrokePaint = new Paint();
        mCircleStrokePaint.setColor(mStrokeColor);
        mCircleStrokePaint.setStyle(Paint.Style.STROKE);
        mCircleStrokePaint.setStrokeWidth(15);
        mWavePaint.setAntiAlias(true);

        mCircleFillPaint = new Paint();
        mCircleFillPaint.setColor(mFillColor);
        mCircleFillPaint.setStyle(Paint.Style.FILL);
        mWavePaint.setAntiAlias(true);
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WaveProgress);
        mWaveColor = ta.getColor(R.styleable.WaveProgress_waveColor, mWaveColor);
        mFillColor = ta.getColor(R.styleable.WaveProgress_fillColor, mFillColor);
        mStrokeColor = ta.getColor(R.styleable.WaveProgress_strokeColor, mStrokeColor);
        mTextColor = ta.getColor(R.styleable.WaveProgress_textColor, mTextColor);
        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWavePath.reset();
        mCirclePath.reset();
        mWavePath.moveTo(-mOneWaveLength + mAimDx, getHeight() - mAnimDY); //刚开始时，起始点为X方向往左移动一个波长
        for (int waveLength = 0; waveLength < getWidth() + mOneWaveLength; waveLength += mOneWaveLength) {//画满整个View的宽度（前后各多出一个波长）
            mWavePath.rQuadTo(mOneWaveLength / 4, mOneWaveHeight, mOneWaveLength / 2, 0); //波浪前半个波长
            mWavePath.rQuadTo(mOneWaveLength / 4,  -mOneWaveHeight, mOneWaveLength / 2, 0); //波浪后半个波长
        }
        mWavePath.lineTo(getWidth(), getHeight());
        mWavePath.lineTo(0, getHeight());
        mWavePath.close();

        //Clip the canvas into round shape
        mCirclePath.addCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, Path.Direction.CW);
        canvas.clipPath(mCirclePath);
        //Fill the circle
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mCircleFillPaint);
        //Draw the wave
        canvas.drawPath(mWavePath, mWavePaint);
        //Stroke the circle
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mCircleStrokePaint);

        //Start the wave animation
        startWaveAnim();
        startProgressAnim();
    }

    public void startWaveAnim() {
        if (mWaveAnimator != null && mWaveAnimator.isRunning()) return;
        mWaveAnimator = ValueAnimator.ofInt(0, mOneWaveLength);
        mWaveAnimator.setDuration(800);
        mWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mWaveAnimator.setInterpolator(new LinearInterpolator());
        mWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAimDx = (int)animation.getAnimatedValue();
                postInvalidate();
            }
        });
        mWaveAnimator.start();
    }

    public void startProgressAnim() {
        if (mProgressAnimator != null && mProgressAnimator.isRunning()) return;
        mProgressAnimator = ValueAnimator.ofInt(10, getHeight() + mOneWaveHeight);
        mProgressAnimator.setDuration(20000);
        mProgressAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mProgressAnimator.setInterpolator(new LinearInterpolator());
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimDY = (int)animation.getAnimatedValue();
            }
        });
        mProgressAnimator.start();
    }
}
