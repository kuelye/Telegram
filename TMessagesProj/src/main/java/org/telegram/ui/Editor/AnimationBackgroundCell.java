package org.telegram.ui.Editor;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedBackgroundView;
import org.telegram.ui.Components.LayoutHelper;

import static org.telegram.messenger.AndroidUtilities.dp;

public class AnimationBackgroundCell extends FrameLayout {

    private final static int HEIGHT = dp(156);

    private final AnimatedBackgroundView backgroundView;

    public AnimationBackgroundCell(@NonNull Context context) {
        super(context);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        backgroundView = new AnimatedBackgroundView(getContext());
        addView(backgroundView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.NO_GRAVITY, 0, 12, 0, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(LayoutHelper.measureExactlySpec(widthMeasureSpec), LayoutHelper.measureExactly(HEIGHT));
    }
}
