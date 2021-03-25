package org.telegram.ui.Editor;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.ui.Components.LayoutHelper;

public class BackgroundAnimationEditorView extends FrameLayout {
    public BackgroundAnimationEditorView(@NonNull Context context) {
        super(context);

        TextView textView = new TextView(context);
        textView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        textView.setText(AnimationType.BACKGROUND.getTitle());
        addView(textView);
    }
}
