package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.animation.AnimationController;
import org.telegram.messenger.animation.AnimationType;
import org.telegram.messenger.animation.BackgroundAnimation;

public class AnimatedBackgroundView extends View implements AnimationController.OnAnimationChangedListener {

    private final static PointF[] DEFAULT_POINTS = { new PointF(0.35f, 0.25f), new PointF(0.82f, 0.08f), new PointF(0.65f, 0.75f), new PointF(0.18f, 0.92f)};
    private final static float RADIUS_FACTOR = 0.05f;
    private final static int STROKE_WIDTH = AndroidUtilities.dp(2);
    private final static int STROKE_COLOR = Color.WHITE;

    private final Paint fillPaint;
    private final Paint strokePaint;
    private BackgroundAnimation animation;

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
        for (int i = 0; i < BackgroundAnimation.COLORS_COUNT; ++i) {
            fillPaint.setColor(animation.getColor(i));
            float x = canvas.getWidth() * DEFAULT_POINTS[i].x;
            float y = canvas.getHeight() * DEFAULT_POINTS[i].y;
            canvas.drawCircle(x, y, radius, fillPaint);
            canvas.drawCircle(x, y, radius, strokePaint);
        }
    }

    @Override
    public void onAnimationChanged() {
        updateAnimation();
    }

    private void updateAnimation() {
        animation = AnimationController.getBackgroundAnimation();
        invalidate();
    }
}
