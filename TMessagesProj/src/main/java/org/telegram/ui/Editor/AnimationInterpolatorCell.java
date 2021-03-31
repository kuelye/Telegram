package org.telegram.ui.Editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.zxing.common.detector.MathUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.animation.Interpolator;
import org.telegram.ui.ActionBar.Theme;

public class AnimationInterpolatorCell extends FrameLayout {

    private final static int HEIGHT = AndroidUtilities.dp(216);

    private final static int STROKE_WIDTH = AndroidUtilities.dp(2);
    private final static int TIME_CIRCLE_RADIUS = AndroidUtilities.dp(4);
    private final static int TIME_POINTER_RX = AndroidUtilities.dp(6);
    private final static int TIME_POINTER_RY = AndroidUtilities.dp(12);
    private final static int CONTROL_POINTER_RADIUS = AndroidUtilities.dp(9);
    private final static int HORIZONTAL_PADDING = AndroidUtilities.dp(28);
    private final static int SHADOW_OFFSET = AndroidUtilities.dp(1);
    private final static int TEXT_PADDING = AndroidUtilities.dp(4);

    private final static int START_DRAG_DISTANCE = AndroidUtilities.dp(20);

    private final Rect rect = new Rect();
    private final RectF rectF = new RectF();
    private final Path path = new Path();

    private final Drawable circleDrawable;
    private final Paint strokePaint;
    private final Paint dottedStrokePaint;
    private final Paint fillPaint;
    private final Paint textPaint;

    private float[] cs = { 1.0f, 1.0f };
    private float[] ts = { 0.0f, 1.0f };

    private final Point[] tps = new Point[2];
    private final Point[] cps = new Point[4];

    private DragType dragType = null;

    private int duration;

    private OnInterpolatorChangedListener onInterpolatorChangedListener;

    public AnimationInterpolatorCell(@NonNull Context context) {
        super(context);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        circleDrawable = context.getResources().getDrawable(R.drawable.knob_shadow).mutate();
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(STROKE_WIDTH);
        dottedStrokePaint = new Paint();
        dottedStrokePaint.setAntiAlias(true);
        dottedStrokePaint.setStyle(Paint.Style.STROKE);
        dottedStrokePaint.setStrokeWidth(STROKE_WIDTH);
        dottedStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        dottedStrokePaint.setPathEffect(new DashPathEffect(new float[] {1, TIME_CIRCLE_RADIUS * 2}, 0));
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(AndroidUtilities.dp(16));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(HEIGHT, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int x = HORIZONTAL_PADDING;
        int y = AndroidUtilities.dp(42);
        int activeWidth = width - 2 * x;
        int activeHeight = height - 2 * y;
        for (int i = 0; i < 2; ++i) {
            int xt = (int) (x + ts[i] * activeWidth);
            int yt = y + activeHeight / 2;
            drawTimeBorder(canvas, xt, y);
            drawTimePointer(canvas, xt, yt, TIME_POINTER_RX, TIME_POINTER_RY);
            tps[i] = new Point(xt, yt);
        }

        activeWidth = tps[1].x - tps[0].x;
        for (int i = 0; i < 2; ++i) {
            float c = i == 1 ? 1 - cs[i] : cs[i];
            int yc = i == 0 ? y + activeHeight : y;
            cps[i * 2] = new Point(i == 0 ? tps[0].x : tps[1].x, yc);
            cps[i * 2 + 1] = new Point((int) (tps[0].x + c * activeWidth), yc);
        }

        strokePaint.setColor(0xFFEBEDF0);
        path.rewind();
        path.moveTo(cps[0].x, cps[0].y);
        path.cubicTo(cps[1].x, cps[1].y, cps[3].x, cps[3].y, cps[2].x, cps[3].y);
        canvas.drawPath(path, strokePaint);

        for (int i = 0; i < 2; ++i) {
            Point p0 = cps[i * 2];
            Point p1 = cps[i * 2 + 1];
            strokePaint.setColor(0xFFEBEDF0);
            canvas.drawLine(x, p0.y, width - x, p0.y, strokePaint);
            strokePaint.setColor(0xFF54AAEB);
            canvas.drawLine(p0.x, p0.y, p1.x, p1.y, strokePaint);
        }

        for (int i = 0; i < 2; ++i) {
            drawTimeBorder(canvas, tps[i].x, y);
            drawTimePointer(canvas, tps[i].x, tps[i].y, TIME_POINTER_RX, TIME_POINTER_RY);
        }

        for (int i = 0; i < 2; ++i) {
            Point p = cps[i * 2 + 1];
            drawControlPointer(canvas, p.x, p.y);
        }

        for (int i = 0; i < 2; ++i) {
            String text = LocaleController.formatString("AnimationDurationTemplate", R.string.AnimationDurationTemplate, (int) (ts[i] * duration));
            textPaint.getTextBounds(text, 0, text.length(), rect);
            x = tps[i].x + (i == 0 && ts[i] < 0.25 || i == 1 && ts[i] < 0.75 ? TIME_POINTER_RX + TEXT_PADDING : - TIME_POINTER_RX - TEXT_PADDING - rect.width());
            textPaint.setColor(0xFFFFCD00);
            canvas.drawText(text, x, tps[i].y + (float) rect.height() / 2, textPaint);
            text = (int) Math.round(cs[i] * 100) + "%";
            textPaint.getTextBounds(text, 0, text.length(), rect);
            Point p = cps[i * 2 + 1];
            x = MathUtils.clip((int) (p.x - (float) rect.width() / 2), HORIZONTAL_PADDING - TEXT_PADDING, width - HORIZONTAL_PADDING + TEXT_PADDING - rect.width());
            y =  p.y + (i == 0 ? CONTROL_POINTER_RADIUS + TEXT_PADDING + rect.height() : - CONTROL_POINTER_RADIUS - TEXT_PADDING);
            textPaint.setColor(0xFF54AAEB);
            canvas.drawText(text, x, y, textPaint);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            dragType = null;
            if (Math.abs(event.getY() - cps[1].y) < START_DRAG_DISTANCE) {
                dragType = DragType.CONTROL_0;
            } else if (Math.abs(event.getY() - cps[3].y) < START_DRAG_DISTANCE) {
                dragType = DragType.CONTROL_1;
            } else if (event.getX() < tps[0].x + START_DRAG_DISTANCE) {
                dragType = DragType.TIME_0;
            } else if (event.getX() > tps[1].x - START_DRAG_DISTANCE) {
                dragType = DragType.TIME_1;
            }
            if (dragType != null) {
                requestDisallowInterceptTouchEvent(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN  || event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (dragType != null) {
                switch (dragType) {
                    case TIME_0:
                        ts[0] = MathUtils.clip((event.getX() - HORIZONTAL_PADDING) / (getMeasuredWidth() - HORIZONTAL_PADDING * 2), 0, ts[1]);
                        break;
                    case TIME_1:
                        ts[1] = MathUtils.clip((event.getX() - HORIZONTAL_PADDING) / (getMeasuredWidth() - HORIZONTAL_PADDING * 2), ts[0], 1);
                        break;
                    case CONTROL_0:
                        cs[0] = MathUtils.clip((event.getX() - tps[0].x) / (tps[1].x - tps[0].x), 0, 1);
                        break;
                    case CONTROL_1:
                        cs[1] = MathUtils.clip((tps[1].x - event.getX()) / (tps[1].x - tps[0].x), 0, 1);
                        break;
                }
                invalidate();
                return true;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            dragType = null;
        }
        return super.onTouchEvent(event);
    }

    public void setDuration(int duration) {
        this.duration = duration;
        invalidate();
    }

    public void setInterpolatorParams(Interpolator interpolator) {
        cs = interpolator.getCs();
        ts = interpolator.getTs();
    }

    private void setInterpolatorParams(Interpolator interpolator, boolean internal) {
        cs = interpolator.getCs();
        ts = interpolator.getTs();
        if (internal && onInterpolatorChangedListener != null) {
            onInterpolatorChangedListener.onInterpolatorParamsChanged(cs, ts);
        }
    }

    public void setOnInterpolatorChangedListener(OnInterpolatorChangedListener listener) {
        onInterpolatorChangedListener = listener;
    }

    private void drawTimeBorder(Canvas canvas, int x, int top) {
        fillPaint.setColor(0xFFFFCD00); // TODO color
        int bottom = canvas.getHeight() - top;
        canvas.drawCircle(x, top, TIME_CIRCLE_RADIUS, fillPaint);
        canvas.drawCircle(x, bottom, TIME_CIRCLE_RADIUS, fillPaint);
        strokePaint.setColor(0xffffffff); // TODO color
        canvas.drawCircle(x, top, TIME_CIRCLE_RADIUS, strokePaint);
        canvas.drawCircle(x, bottom, TIME_CIRCLE_RADIUS, strokePaint);
        dottedStrokePaint.setColor(0xFFFFCD00); // TODO color
        canvas.drawLine(x, top, x, bottom, dottedStrokePaint);
    }

    private void drawTimePointer(Canvas canvas, int x, int y, int rx, int ry) {
        fillPaint.setColor(0xFFEBEDF0); // TODO color
        int s = SHADOW_OFFSET / 2;
        rectF.set(x - rx - s, y - ry - s, x + rx + s, y + ry + s);
        canvas.drawRoundRect(rectF, rx, rx, fillPaint);
        fillPaint.setColor(0xffffffff); // TODO color
        rectF.set(x - rx, y - ry, x + rx, y + ry);
        canvas.drawRoundRect(rectF, rx, rx, fillPaint);
    }

    private void drawControlPointer(Canvas canvas, int x, int y) {
        circleDrawable.setBounds(x - CONTROL_POINTER_RADIUS - SHADOW_OFFSET, y - CONTROL_POINTER_RADIUS - SHADOW_OFFSET
                , x + CONTROL_POINTER_RADIUS + SHADOW_OFFSET, y + CONTROL_POINTER_RADIUS + SHADOW_OFFSET);
        circleDrawable.draw(canvas);
        fillPaint.setColor(0xffffffff); // TODO color
        canvas.drawCircle(x, y, CONTROL_POINTER_RADIUS, fillPaint);
    }

    enum DragType {
        TIME_0,
        TIME_1,
        CONTROL_0,
        CONTROL_1
    }

    public interface OnInterpolatorChangedListener {
        void onInterpolatorParamsChanged(float[] cs, float[] ts);
    }
}
