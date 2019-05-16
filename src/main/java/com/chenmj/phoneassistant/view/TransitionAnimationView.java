package com.chenmj.phoneassistant.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by mikechenmj on 18-7-18.
 */

public class TransitionAnimationView extends View {

    private Point mCenterCirclePos;
    private int mMaxRadius;
    private boolean mInverse;
    private Paint mPaint;
    private float mScale = 1f;
    private Path.FillType mPathFillType = Path.FillType.EVEN_ODD;
    private Bitmap mTransitionForeground;
    private Bitmap mTransitionBackground;

    public static final int DURATION = 600;
    public static final String EXTRA_TRANSITION_BITMAP = "com.chenmj.phoneassistant.EXTRA_TRANSITION_BITMAP";

    public TransitionAnimationView(Context context) {
        this(context, null);
    }

    public TransitionAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public void setScale(float scale) {
        mScale = scale;
        invalidate();
    }

    public float getScale() {
        return mScale;
    }

    public void setTransitionForeground(Bitmap transitionForeground) {
        mTransitionForeground = transitionForeground;
    }

    public Bitmap getTransitionForeground() {
        return mTransitionForeground;
    }

    public void setTransitionBackground(Bitmap transitionBackground) {
        mTransitionBackground = transitionBackground;
    }

    public Bitmap getTransitionBackground() {
        return mTransitionBackground;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getMatchParentSize(widthMeasureSpec),
                getMatchParentSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTransition(canvas);
    }

    private void drawTransition(Canvas canvas) {
        Bitmap transitionForeground = mTransitionForeground;
        if (transitionForeground == null) {
            return;
        }

        Bitmap transitionBackground = mTransitionBackground;
        if (transitionBackground != null) {
            canvas.drawBitmap(mTransitionBackground, null, new Rect(0, 0, getWidth(), getHeight()), null);
        }

        if (mCenterCirclePos == null) {
            canvas.drawBitmap(transitionForeground, null, new Rect(0, 0, getWidth(), getHeight()), null);
            return;
        }

        int radius = (int) (mMaxRadius * mScale);
        int layerLeft;
        int layerTop;
        int layerRight;
        int layerBottom;
        if (mInverse) {
            layerLeft = mCenterCirclePos.x - radius;
            layerTop = mCenterCirclePos.y - radius;
            layerRight = mCenterCirclePos.x + radius;
            layerBottom = mCenterCirclePos.y + radius;
        } else {
            layerLeft = 0;
            layerTop = 0;
            layerRight = getWidth();
            layerBottom = getHeight();
        }
        RectF rectF = new RectF(layerLeft, layerTop, layerRight, layerBottom);
        int saveCount = canvas.saveLayer(rectF, new Paint(), canvas.ALL_SAVE_FLAG);
        canvas.drawBitmap(transitionForeground, null, new Rect(0, 0, getWidth(), getHeight()), null);
        Path path = new Path();
        path.setFillType(mPathFillType);
        path.addCircle(mCenterCirclePos.x, mCenterCirclePos.y, radius, Path.Direction.CW);
        canvas.drawPath(path, mPaint);
        canvas.restoreToCount(saveCount);
    }

    private int getMatchParentSize(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    private void computeCenterPosAndRadius(Rect rect) {
        int centerX = rect.left + rect.width() / 2;
        int centerY = rect.top + rect.height() / 2;
        computeCenterPosAndRadius(centerX, centerY);
    }

    private void computeCenterPosAndRadius(int x, int y) {
        mCenterCirclePos = new Point(x, y);
        Point centerCirclePos = mCenterCirclePos;
        double leftTopDiagonal = Math.sqrt(Math.pow(centerCirclePos.x, 2) + Math.pow(centerCirclePos.y, 2));
        double rightTopDiagonal = Math.sqrt(Math.pow(getWidth() - centerCirclePos.x, 2) + Math.pow(centerCirclePos.y, 2));
        double leftBottomDiagonal = Math.sqrt(Math.pow(centerCirclePos.x, 2) + Math.pow(getHeight() - centerCirclePos.y, 2));
        double rightBottomDiagonal = Math.sqrt(Math.pow(getWidth() - centerCirclePos.x, 2) + Math.pow(getHeight() - centerCirclePos.y, 2));
        mMaxRadius = (int) Math.max(Math.max(leftTopDiagonal, rightTopDiagonal), Math.max(leftBottomDiagonal, rightBottomDiagonal));
        Log.i("MCJ", "mMaxRadius: " + mMaxRadius);
    }

    public void startTransitionAnimation(@Nullable ValueAnimator.AnimatorListener animatorListener,
                                         long duration, Rect rect, boolean inverse, Bitmap transitionBitmap) {
        mTransitionForeground = transitionBitmap;
        computeCenterPosAndRadius(rect);
        startTransitionAnimation(animatorListener, duration, inverse);
    }

    public void startTransitionAnimation(@Nullable ValueAnimator.AnimatorListener animatorListener,
                                         long duration, int x, int y, boolean inverse, Bitmap transitionBitmap) {
        mTransitionForeground = transitionBitmap;
        computeCenterPosAndRadius(x, y);
        startTransitionAnimation(animatorListener, duration, inverse);
    }

    private void startTransitionAnimation(@Nullable ValueAnimator.AnimatorListener animatorListener,
                                          long duration, boolean inverse) {
        mInverse = inverse;
        float[] values;
        if (inverse) {
            values = new float[]{1, 0};
            mPathFillType = Path.FillType.INVERSE_EVEN_ODD;
        } else {
            values = new float[]{0, 1};
            mPathFillType = Path.FillType.EVEN_ODD;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(values);
        valueAnimator.setDuration(duration > 0 ? duration : DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setScale((Float) animation.getAnimatedValue());
            }
        });
        if (animatorListener != null) {
            valueAnimator.addListener(animatorListener);
        }
        valueAnimator.start();
    }

}
