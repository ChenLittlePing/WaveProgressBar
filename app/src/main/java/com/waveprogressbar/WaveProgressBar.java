package com.waveprogressbar;

import android.animation.Animator;
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
import android.widget.Scroller;

/**
 * A loading view with wave animation
 *
 * @author ChenLittlePing (562818444@qq.com)
 * @version v1.0
 * @Datetime 2017-05-10 15:00
 */

public class WaveProgressBar extends View {

    private Context mContext;

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

    /**The paint draw text*/
    private Paint mTextPaint;

    /**The wave length*/
    private int mOneWaveLength = 0;

    /**The wave height*/
    private int mOneWaveHeight = 0;

    /**The wave's scrolling distance in X direction*/
    private float mAnimDx = 0;

    /**The wave's scrolling distance in Y direction*/
    private int mAnimDY = 0;

    /**Wave animator scroller*/
    private Scroller mScroller;

    /**Progress animator*/
    private ValueAnimator mProgressAnimator;

    /**Duration to move one wave length*/
    private int mWaveDuration = 2500;

    /**Loading progress*/
    private float mProgress = 0;

    private int mWaveColor = Color.GREEN;
    private int mFillColor = Color.WHITE;
    private int mStrokeColor = Color.GREEN;
    private int mTextColor = Color.WHITE;
    private float mStrokeWidth = 15;
    private float mTextSize = 40;
    private boolean mIsTextAnim = false;
    private boolean mStopTextAnimateAtCenter = true;
    private boolean mIsTextOverMiddle = false;

    public WaveProgressBar(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public WaveProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getAttrs(context, attrs);
        init();
    }

    public WaveProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        getAttrs(context, attrs);
        init();
    }

    private void init() {
        mWavePath = new Path();
        mCirclePath = new Path();

        //init wave paint
        mWavePaint = new Paint();
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mWavePaint.setAntiAlias(true);

        //init stroke paint
        mCircleStrokePaint = new Paint();
        mCircleStrokePaint.setColor(mStrokeColor);
        mCircleStrokePaint.setStyle(Paint.Style.STROKE);
        mCircleStrokePaint.setStrokeWidth(mStrokeWidth);
        mWavePaint.setAntiAlias(true);

        //init fill paint
        mCircleFillPaint = new Paint();
        mCircleFillPaint.setColor(mFillColor);
        mCircleFillPaint.setStyle(Paint.Style.FILL);
        mWavePaint.setAntiAlias(true);

        //init text paint
        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WaveProgressBar);

        mWaveColor = ta.getColor(R.styleable.WaveProgressBar_waveColor, mWaveColor);
        mFillColor = ta.getColor(R.styleable.WaveProgressBar_fillColor, mFillColor);
        mStrokeColor = ta.getColor(R.styleable.WaveProgressBar_strokeColor, mStrokeColor);
        mTextColor = ta.getColor(R.styleable.WaveProgressBar_textColor, mTextColor);
        mStrokeWidth = ta.getDimension(R.styleable.WaveProgressBar_strokeWidth, mStrokeWidth);

        mTextSize = sp2px(context, mTextSize);
        mTextSize = ta.getDimension(R.styleable.WaveProgressBar_textSize, mTextSize);

        ta.recycle();
    }

    /**
     * Change sp to px
     */
    private static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        clipCanvasIntoCircle(canvas);
        drawFill(canvas);
        drawWave(canvas);
        drawStroke(canvas);
        drawText(canvas);
        startAnim();
    }

    /**
     * Clip the canvas into round shape
     */
    private void clipCanvasIntoCircle(Canvas canvas) {
        mCirclePath.reset();
        mCirclePath.addCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, Path.Direction.CW);
        canvas.clipPath(mCirclePath);
    }

    /**
     * Fill color to the circle
     */
    private void drawFill(Canvas canvas) {
        //Fill the circle
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mCircleFillPaint);
    }

    /**
     * Draw the wave
     */
    private void drawWave(Canvas canvas) {
        if (mOneWaveLength == 0) mOneWaveLength = getWidth() * 5 / 3; //default wave length
        if (mOneWaveHeight == 0) mOneWaveHeight = mOneWaveLength / 10; //default wave height

        if (mProgressAnimator == null) {
            mAnimDY = Math.round(mProgress/100 * (getHeight() + mOneWaveHeight));
        }

        mWavePath.reset();

        //刚开始时，起始点为X方向往左移动一个波长，Y方向在View的高往下移动半个波峰高度，
        // 即波浪的升降范围为（-mOneWaveHeight/2 ~ getHeight(()+mOneWaveHeight/2）
        mWavePath.moveTo(-mOneWaveLength + mAnimDx, (getHeight() + mOneWaveHeight/2) - mAnimDY);

        for (int waveLength = 0; waveLength < getWidth() + mOneWaveLength; waveLength += mOneWaveLength) {//画满整个View的宽度（前后各多出一个波长）
            mWavePath.rQuadTo(mOneWaveLength / 4, mOneWaveHeight, mOneWaveLength / 2, 0); //波浪前半个波长
            mWavePath.rQuadTo(mOneWaveLength / 4,  -mOneWaveHeight, mOneWaveLength / 2, 0); //波浪后半个波长
        }
        mWavePath.lineTo(getWidth(), getHeight());
        mWavePath.lineTo(0, getHeight());
        mWavePath.close();
        //Draw the wave
        canvas.drawPath(mWavePath, mWavePaint);
    }

    /**
     * Draw the circle stroke
     */
    private void drawStroke(Canvas canvas) {
        //Stroke the circle
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mCircleStrokePaint);
    }

    /**
     * Draw progress text
     */
    private void drawText(Canvas canvas) {
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        int baseline = (getMeasuredHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        if (mIsTextAnim && !mIsTextOverMiddle) {
            int y = (getHeight() + mOneWaveHeight / 2) - mAnimDY - getTextYPos();
            if (mStopTextAnimateAtCenter && (getHeight() + mOneWaveHeight/2) - mAnimDY < baseline) {
                if (Math.abs(y - baseline) < 3) { //stop at center
                    mIsTextOverMiddle = true;
                }
            }
            canvas.drawText(Math.round(mProgress) + "%", getWidth() / 2, y, mTextPaint);
        } else {
            canvas.drawText(Math.round(mProgress) + "%", getWidth()/2, baseline, mTextPaint);
        }
    }

    /**
     * Calculate the progress text Y position, when {@link #mIsTextAnim} is true
     * <p>二阶贝塞尔曲线坐标计算公式：P(t) = (1-t)²P0 + 2t(1-t)P1 + t²P2
     * <p>其中:
     * <p>P为曲线上的点，即要计算的点X或者Y
     * <p>t范围为1-0 (此处转换为P点X与半个波长的比例)
     * <p>P0为 起始点坐标的X或者Y
     * <p>P1为 控制点坐标的X或者Y
     * <p>P0为 结束点坐标的X或者Y
     */
    private int getTextYPos() {
        int y;
        float t;
        if ((mAnimDx - (getWidth() / 2) % (mOneWaveLength)) > (mOneWaveLength / 2)) { //位置在半个波长以内，并且移动距离 超过 指定位置大于半个波长
            t = (mOneWaveLength + (getWidth() / 2)%mOneWaveLength - mAnimDx) * 1.0f / (mOneWaveLength / 2);
        } else if (((getWidth() / 2) % (mOneWaveLength) - mAnimDx) > (mOneWaveLength / 2)) { //指定位置大于移动距离在半个波长以上
            t = -((getWidth() / 2) % (mOneWaveLength) - mAnimDx - mOneWaveLength / 2) * 1.0f / (mOneWaveLength / 2);
        } else { //位置在半个波长以内，并且移动距离 不超过 指定的位置半个波长
            t = (((getWidth() / 2) % mOneWaveLength) - mAnimDx) * 1.0f / (mOneWaveLength / 2);
        }
        if (t > 0) {
            y = (int) (/*(Math.pow((1 - t), 2) * 0) + */(2 * t * (1 - t) * (-mOneWaveHeight))/* + Math.pow(t, 2) * 0*/); //前后两项Y坐标为0可以不计算
        } else {
            t = 1 + t;
            y = (int) (/*(Math.pow((1 - t), 2) * 0) + */(2 * t * (1 - t) * mOneWaveHeight)/* + Math.pow(t, 2) * 0*/);//前后两项Y坐标为0可以不计算
        }
        return y;
    }

    /**
     * Start wave animation
     */
    private void startAnim() {
        startWaveAnim();
//        startProgressAnim();
    }

    /**
     * Calculate the wave moving speed according to current loading progress
     * <p><b>Higher progress, lower speed</b>
     * @return wave moving speed
     */
    private int calculateWaveSpeed() {
        int speed = Math.round(mProgress / 100 * mWaveDuration);
        if (speed > mWaveDuration) speed = mWaveDuration;
        else if (speed < 1000) speed = 1000;
        return speed;
    }

    /**
     * Start moving wave
     */
    private void startWaveAnim() {
        if (mScroller == null) {
            mScroller = new Scroller(mContext, new LinearInterpolator());
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    performWaveAnim();
                }
            }, 400);
        } else {
            performWaveAnim();
        }
    }

    /**
     * Perform the wave animation
     */
    private void performWaveAnim() {
        if (mScroller != null && mScroller.isFinished()) {
            mScroller.startScroll(0, 0, mOneWaveLength, 0, calculateWaveSpeed());
            postInvalidate();
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller != null && mScroller.computeScrollOffset() && !mScroller.isFinished()) {
            mAnimDx = mScroller.getCurrX();
            postInvalidate();
        }
        super.computeScroll();
    }

    /**
     * Automatically raise the wave, when the progress is 100%, start form 0% again.
     */
    public void startProgressAnim() {
        if (mProgressAnimator != null && mProgressAnimator.isRunning()) return;
        mProgressAnimator = ValueAnimator.ofInt(0, getHeight() + mOneWaveHeight);
        mProgressAnimator.setDuration(20000);
        mProgressAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mProgressAnimator.setInterpolator(new LinearInterpolator());
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimDY = (int)animation.getAnimatedValue();
                mProgress = mAnimDY * 1.0f / (getHeight() + mOneWaveHeight) * 100;
            }
        });
        mProgressAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                mIsTextOverMiddle = false;
            }
        });
        mProgressAnimator.start();
    }

    /**
     * Set loading progress by percent.
     * @param percent loading progress
     */
    public void setProgress(int percent) {
        mProgress = percent;
    }

    /**
     * Set the wave length
     */
    public void setWaveLength(int length) {
        mOneWaveLength = length;
    }

    /**
     * Set the wave length
     */
    public void setWaveHeight(int height) {
        mOneWaveHeight = height;
    }

    /**
     * Animate the text by the wave
     * @param anim true: animate, false:draw in center
     * @param stopAtMiddle true: stop when text arrive at view center, false: never stop
     */
    public void AnimateText(boolean anim, boolean stopAtMiddle) {
        mIsTextAnim = anim;
        mStopTextAnimateAtCenter = stopAtMiddle;
    }
}
