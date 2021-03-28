package org.telegram.ui.Components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.animation.AnimationController;
import org.telegram.messenger.animation.AnimationType;
import org.telegram.messenger.animation.BackgroundAnimation;
import org.telegram.messenger.animation.Interpolator;

public class AnimatedBackgroundView extends View implements AnimationController.OnAnimationChangedListener {

    private final static PointF[] DEFAULT_POINTS = { new PointF(0.35f, 0.25f), new PointF(0.82f, 0.08f), new PointF(0.65f, 0.75f), new PointF(0.18f, 0.92f)};
    private final static float RADIUS_FACTOR = 0.05f;
    private final static int STROKE_WIDTH = AndroidUtilities.dp(2);
    private final static int STROKE_COLOR = Color.WHITE;

    private final Paint fillPaint;
    private final Paint strokePaint;
    private BackgroundAnimation animation;

    private ValueAnimator valueAnimator;
    private float state = 0f;

    public AnimatedBackgroundView(Context context) {
        super(context);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(STROKE_WIDTH);
        strokePaint.setColor(STROKE_COLOR);

        updateAnimation();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AnimationController.addOnAnimationChangedListener(AnimationType.BACKGROUND, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AnimationController.removeOnAnimationChangedListener(AnimationType.BACKGROUND, this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = canvas.getWidth() * RADIUS_FACTOR;
        for (int i = 0; i < BackgroundAnimation.POINTS_COUNT; ++i) {
            fillPaint.setColor(animation.getColor(i));
            PointF point = getPoint(i);
            float x = canvas.getWidth() * point.x;
            float y = canvas.getHeight() * point.y;
            canvas.drawCircle(x, y, radius, fillPaint);
            canvas.drawCircle(x, y, radius, strokePaint);
        }
    }

    @Override
    public void onAnimationChanged() {
        updateAnimation();
    }

    public void animate(Interpolator interpolator) {
        if (valueAnimator != null && valueAnimator.isRunning()) return;
        float startState = state;
        valueAnimator = ValueAnimator.ofFloat(0.0f, 0.5f).setDuration(1000);
        valueAnimator.addUpdateListener(animation -> {
            state = startState + (float) animation.getAnimatedValue();
            invalidate();
        });
        valueAnimator.start();
    }

    private void updateAnimation() {
        animation = AnimationController.getBackgroundAnimation();
        invalidate();
    }

    private PointF getPoint(int i) {
        int s = (int) Math.floor(state * 2) / 2;
        float f = Math.abs(state - s);
        int fromI = clip(i - s);
        int toI = clip(fromI - 1);
        return new PointF(
            AndroidUtilities.lerp(DEFAULT_POINTS[fromI].x, DEFAULT_POINTS[toI].x, f),
            AndroidUtilities.lerp(DEFAULT_POINTS[fromI].y, DEFAULT_POINTS[toI].y, f)
        );
    }

    private int clip(int i) {
        i = i % BackgroundAnimation.POINTS_COUNT;
        if (i < 0) i += BackgroundAnimation.POINTS_COUNT;
        return i;
    }
}
