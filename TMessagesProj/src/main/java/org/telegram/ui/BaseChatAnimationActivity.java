package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Editor.BaseAnimationEditorView;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static org.telegram.messenger.AndroidUtilities.dp;

public abstract class BaseChatAnimationActivity extends BaseFragment {

    protected BaseAnimationEditorView editorView;

    @CallSuper
    @Override
    public View createView(Context context) {
        // toolbar
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAddToContainer(false);

        // content
        ContentView contentView = new ContentView(context);
        editorView = createEditorView();
        editorView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        contentView.addView(editorView);
        contentView.addView(actionBar);
        fragmentView = contentView;

        return fragmentView;
    }

    protected abstract BaseAnimationEditorView createEditorView();

    protected class ContentView extends FrameLayout {

        public ContentView(Context context) {
            super(context);
        }

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            boolean result = super.drawChild(canvas, child, drawingTime);
            if (child == actionBar && parentLayout != null) {
                parentLayout.drawHeaderShadow(canvas, 255, actionBar.getHeight() + dp(44));
            }
            return result;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(widthSize, heightSize);
            measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);
            measureChildWithMargins(editorView, widthMeasureSpec, 0, makeMeasureSpec(
                    heightSize - actionBar.getMeasuredHeight(), View.MeasureSpec.EXACTLY), 0);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();
                int childTop = 0;
                if (child == editorView) {
                    childTop = actionBar.getMeasuredHeight();
                }
                child.layout(0, childTop, width, childTop + height);
            }
        }
    }
}
