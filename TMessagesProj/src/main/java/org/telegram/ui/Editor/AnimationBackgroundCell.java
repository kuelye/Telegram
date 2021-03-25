package org.telegram.ui.Editor;

import android.content.Context;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.ui.ActionBar.Theme;

import static org.telegram.messenger.AndroidUtilities.dp;

public class AnimationBackgroundCell extends FrameLayout {

    private final static int HEIGHT = dp(144);

    public AnimationBackgroundCell(@NonNull Context context) {
        super(context);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(HEIGHT, MeasureSpec.EXACTLY));
    }
}
