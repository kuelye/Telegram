package org.telegram.ui.Editor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.zxing.common.detector.MathUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

public class AnimationInterpolatorCell extends FrameLayout {

    private final static int HEIGHT = AndroidUtilities.dp(216);

    private final static int STROKE_WIDTH = AndroidUtilities.dp(2);
    private final static int TIME_CIRCLE_RADIUS = AndroidUtilities.dp(3);
    private final static int CONTROL_POINTER_RADIUS = AndroidUtilities.dp(9);
    private final static int HORIZONTAL_PADDING = AndroidUtilities.dp(28);
    private final static int CONTROL_LINE_PADDING = AndroidUtilities.dp(5);
    private final static int SHADOW_OFFSET = AndroidUtilities.dp(1);

    private final static int START_DRAG_DISTANCE = AndroidUtilities.dp(24);

    private final RectF rect = new RectF();
    private final Path path = new Path();

    private Drawable circleDrawable;
    private final Paint strokePaint;
    private final Paint dottedStrokePaint;
    private final Paint fillPaint;

    private float[] cs = { 1.0f, 1.0f };
    private float[] ts = { 0.0f, 1.0f };

    private Point[] tps = new Point[2];
    private Point[] cps = new Point[4];

    private DragType dragType = null;
    private MotionEvent dragDownEvent = null;

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
            drawTimePointer(canvas, xt, yt, AndroidUtilities.dp(6), AndroidUtilities.dp(12));
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
            drawControlPointer(canvas, p1.x, p1.y);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            dragType = null;
            if (MathUtils.distance(event.getX(), event.getY(), tps[0].x, tps[0].y) < START_DRAG_DISTANCE) {
                dragType = DragType.TIME_0;
            } else if (MathUtils.distance(event.getX(), event.getY(), tps[1].x, tps[1].y) < START_DRAG_DISTANCE) {
                dragType = DragType.TIME_1;
            } else if (MathUtils.distance(event.getX(), event.getY(), cps[1].x, cps[1].y) < START_DRAG_DISTANCE) {
                dragType = DragType.CONTROL_0;
            } else if (MathUtils.distance(event.getX(), event.getY(), cps[3].x, cps[3].y) < START_DRAG_DISTANCE) {
                dragType = DragType.CONTROL_1;
            }
            if (dragType != null) {
                dragDownEvent = event;
                requestDisallowInterceptTouchEvent(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
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
            }
        }

        if (dragType != null) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    private void drawTimeBorder(Canvas canvas, int x, int top) {
        fillPaint.setColor(0xFFFFCD00); // TODO color
        int bottom = canvas.getHeight() - top;
        canvas.drawCircle(x, top, TIME_CIRCLE_RADIUS, fillPaint);
        canvas.drawCircle(x, bottom, TIME_CIRCLE_RADIUS, fillPaint);
        dottedStrokePaint.setColor(0xFFFFCD00); // TODO color
        canvas.drawLine(x, top, x, bottom, dottedStrokePaint);
    }

//    private void drawControlBorder(Canvas canvas, int ) {
//        strokePaint.setColor(0xFFEBEDF0);
//        canvas.drawLine();
//    }

    private void drawTimePointer(Canvas canvas, int x, int y, int rx, int ry) {
        circleDrawable.setBounds(x - rx - SHADOW_OFFSET, y - ry - SHADOW_OFFSET, x + rx + SHADOW_OFFSET, y + ry + SHADOW_OFFSET);
        circleDrawable.draw(canvas);
        fillPaint.setColor(0xffffffff); // TODO color
        rect.set(x - rx, y - ry, x + rx, y + ry);
        canvas.drawRoundRect(rect, rx, rx, fillPaint);
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
}
