package org.telegram.ui.Cells;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.view.GravityCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ColorPicker;
import org.telegram.ui.Components.LayoutHelper;

import static org.telegram.messenger.AndroidUtilities.dp;

public class TextColorPickerCell extends TextSettingsCell {

    private final static int DEFAULT_COLOR = Color.BLACK;

    @ColorInt private int color;

    private final RectF rect = new RectF();
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private OnColorAppliedListener onColorAppliedListener;

    private boolean isPickerShown;
    private float changingOverlayAlpha;
    private ValueAnimator changingOverlayAnimator;

    public TextColorPickerCell(Context context) {
        super(context);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        setColor(DEFAULT_COLOR);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        fillPaint.setColor(color);
        rect.set(valueTextView.getLeft() - dp(8), valueTextView.getTop() + dp(8), valueTextView.getRight() + dp(8), valueTextView.getBottom() - dp(8));
        canvas.drawRoundRect(rect, dp(8), dp(8), fillPaint);
        if (changingOverlayAlpha > 0) {
            fillPaint.setColor(Color.BLACK);
            fillPaint.setAlpha((int) (100 * changingOverlayAlpha));
            int d = (int) valueTextView.getPaint().measureText("#");
            rect.set(valueTextView.getLeft() + d - dp(1), valueTextView.getTop() + dp(15), valueTextView.getRight() + dp(1), valueTextView.getBottom() - dp(15));
            canvas.drawRoundRect(rect, dp(3), dp(3), fillPaint);
            fillPaint.setAlpha(255);
        }
        super.dispatchDraw(canvas);
    }

    @Override
    public void setText(String text, boolean divider) {
        super.setText(text, divider);
        updateColor();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (changingOverlayAnimator != null) {
            changingOverlayAnimator.cancel();
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        setColor(color, false);
    }

    public void setColor(@ColorInt int color, boolean internal) {
        this.color = color;
        if (internal && onColorAppliedListener != null) {
            onColorAppliedListener.onColorApplied(color);
        }
        updateColor();
    }

    public void setOnColorAppliedListener(OnColorAppliedListener listener) {
        onColorAppliedListener = listener;
    }

    public void setPickerShown(boolean pickerShown) {
        if (this.isPickerShown == pickerShown) {
            return;
        }

        this.isPickerShown = pickerShown;
        if (changingOverlayAnimator != null) {
            changingOverlayAnimator.cancel();
            changingOverlayAnimator = null;
        }

        float startChangingAlpha = changingOverlayAlpha;
        float endChangingAlpha = pickerShown ? 1 : 0;
        changingOverlayAnimator = ValueAnimator.ofFloat(startChangingAlpha, endChangingAlpha);
        changingOverlayAnimator.setDuration(500);
        changingOverlayAnimator.addUpdateListener(animation -> {
            changingOverlayAlpha = (float) animation.getAnimatedValue();
            invalidate();
        });
        changingOverlayAnimator.start();;
    }

    public void showPicker() {
        if (isPickerShown) {
            return;
        }

        BottomSheet.Builder builder = new BottomSheet.Builder(getContext());
        builder.setApplyTopPadding(false);
        builder.setApplyBottomPadding(false);
        builder.setApplyShadowDrawable(false);
        RelativeLayout container = new RelativeLayout(getContext());
        builder.setCustomView(container);
        BottomSheet bottomSheet = builder.create();
        bottomSheet.setOnDismissListener(dialog -> setPickerShown(false));

        // color pickers
        ColorPicker colorPicker = new ColorPicker(getContext(), false, (color, num, applyNow) -> {}) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                canvas.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1, Theme.dividerPaint);
            }
        };
        colorPicker.setInternalBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        colorPicker.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 256));
        colorPicker.setType(1, false, false, false, false, 0, false);
        colorPicker.setHeaderVisible(false);
        colorPicker.setColor(color, 0);
        colorPicker.setId(android.R.id.content);
        container.addView(colorPicker);

        // buttons
        FrameLayout buttonsLayout = new FrameLayout(getContext());
        buttonsLayout.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
        buttonsLayout.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        buttonsLayout.setLayoutParams(LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, 60, RelativeLayout.BELOW, android.R.id.content));
        container.addView(buttonsLayout);
        buttonsLayout.addView(createButton(LocaleController.getString("Cancel", R.string.Cancel), GravityCompat.START, v -> {
            bottomSheet.dismiss();
            setPickerShown(false);
        }));
        buttonsLayout.addView(createButton(LocaleController.getString("ApplyTheme", R.string.ApplyTheme), GravityCompat.END, v -> {
            setPickerShown(false);
            setColor(colorPicker.getColor(), true);
            bottomSheet.dismiss();
        }));

        setPickerShown(true);
        bottomSheet.show();
    }

    private TextView createButton(String text, int gravity, OnClickListener listener) {
        TextView view = new TextView(getContext());
        view.setText(text);
        view.setOnClickListener(listener);
        view.setAllCaps(true);
        view.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 36, gravity | Gravity.CENTER_VERTICAL));
        view.setGravity(Gravity.CENTER);
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        view.setTextColor(Theme.getColor(Theme.key_dialogButton));
        view.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        view.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
        view.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(Theme.getColor(Theme.key_dialogButton)));
        return view;
    }

    private void updateColor() {
        setValue(String.format("#%06X", (0xFFFFFF & color)));
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        valueTextView.setTextColor(Theme.getColor(hsv[2] < 0.5f ? Theme.key_windowBackgroundWhite :Theme.key_windowBackgroundWhiteBlackText));
    }

    public interface OnColorAppliedListener {
        void onColorApplied(@ColorInt int color);
    }
}
