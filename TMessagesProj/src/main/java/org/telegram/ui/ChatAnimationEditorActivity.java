package org.telegram.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Editor.AnimationEditorView;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static org.telegram.messenger.AndroidUtilities.dp;

public class ChatAnimationEditorActivity extends BaseFragment {

    private final static int SHARE_PARAMETERS_MENU_ITEM_ID = 0;
    private final static int IMPORT_PARAMETERS_MENU_ITEM_ID = 1;
    private final static int RESTORE_TO_DEFAULT_MENU_ITEM_ID = 2;

    private AnimationEditorView editorView;

    @Override
    public View createView(Context context) {
        // toolbar
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("AnimationSettings", R.string.AnimationSettings));
        actionBar.setAddToContainer(false);
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem overflowItem = menu.addItem(0, R.drawable.ic_ab_other);
        overflowItem.addSubItem(SHARE_PARAMETERS_MENU_ITEM_ID
                , LocaleController.getString("ShareParameters", R.string.ShareParameters));
        overflowItem.addSubItem(IMPORT_PARAMETERS_MENU_ITEM_ID
                , LocaleController.getString("ImportParameters", R.string.ImportParameters));
        overflowItem.addSubItem(RESTORE_TO_DEFAULT_MENU_ITEM_ID
                , LocaleController.getString("RestoreToDefault", R.string.RestoreToDefault)
                , Theme.getColor(Theme.key_windowBackgroundWhiteRedText));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                // TODO [CONTEST] implement sharing, importing and restoring to default
                switch (id) {
                    case -1:
                        finishFragment();
                        break;
                }
            }
        });

        ContentView contentView = new ContentView(context);
        editorView = new AnimationEditorView(context);
        editorView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        contentView.addView(editorView);
        contentView.addView(actionBar);
        fragmentView = contentView;

        return fragmentView;
    }

    private class ContentView extends FrameLayout {

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
