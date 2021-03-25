package org.telegram.ui.Editor;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.ui.Components.LayoutHelper;

public class DefaultAnimationEditorView extends FrameLayout {

    private final AnimationType animationType;

    public DefaultAnimationEditorView(@NonNull Context context, AnimationType animationType) {
        super(context);
        this.animationType = animationType;

        TextView textView = new TextView(context);
        textView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
        textView.setText(animationType.getTitle());
        addView(textView);
    }
}
