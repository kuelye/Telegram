package org.telegram.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.animation.AnimationController;
import org.telegram.messenger.animation.AnimationType;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Editor.AnimationEditorPageView;
import org.telegram.ui.Editor.BaseAnimationEditorView;

public class ChatAnimationEditorActivity extends BaseChatAnimationActivity {

    private final static int SHARE_PARAMETERS_MENU_ITEM_ID = 0;
    private final static int IMPORT_PARAMETERS_MENU_ITEM_ID = 1;
    private final static int RESTORE_TO_DEFAULT_MENU_ITEM_ID = 2;

    @Override
    public View createView(Context context) {
        View view = super.createView(context);

        // toolbar
        actionBar.setTitle(LocaleController.getString("AnimationSettings", R.string.AnimationSettings));
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem overflowItem = menu.addItem(0, R.drawable.ic_ab_other);
        overflowItem.addSubItem(SHARE_PARAMETERS_MENU_ITEM_ID, LocaleController.getString("ShareParameters", R.string.ShareParameters));
        overflowItem.addSubItem(IMPORT_PARAMETERS_MENU_ITEM_ID, LocaleController.getString("ImportParameters", R.string.ImportParameters));
        overflowItem.addSubItem(RESTORE_TO_DEFAULT_MENU_ITEM_ID, LocaleController.getString("RestoreToDefault", R.string.RestoreToDefault), Theme.getColor(Theme.key_windowBackgroundWhiteRedText));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                // TODO [CONTEST] implement sharing, importing and restoring to default
                switch (id) {
                    case -1:
                        finishFragment();
                        break;
                    case RESTORE_TO_DEFAULT_MENU_ITEM_ID:
                        AnimationController.restoreToDefault();
                        break;
                }
            }
        });

        return view;
    }

    @Override
    protected BaseAnimationEditorView createEditorView() {
        return new AnimationEditorView(getParentActivity(), () -> presentFragment(new ChatBackgroundAnimationPreviewActivity()));
    }

    @Override
    public void onPause() {
        super.onPause();
        AnimationController.save();
    }

    private static class AnimationEditorView extends BaseAnimationEditorView {

        private final AnimationEditorPageView.Delegate delegate;

        public AnimationEditorView(Context context, AnimationEditorPageView.Delegate delegate) {
            super(context);
            this.delegate = delegate;
            initializeTabs();
        }

        @Override
        protected BaseAdapter createAdapter() {
            return new AnimationEditorView.Adapter(getContext(), getTabsCount());
        }

        @Override
        protected int getTabsCount() {
            return AnimationType.values().length;
        }

        @Override
        protected String getTabTitle(int position) {
            return AnimationType.values()[position].getTitle();
        }

        @Override
        protected void onSamePageSelected(int page) {
            ((AnimationEditorPageView) viewPagerAdapter.getView(page)).smoothScrollToPosition(0);
        }

        private class Adapter extends BaseAdapter {

            Adapter(Context context, int count) {
                super(context, count);
            }

            @Override
            protected View instantiateView(int position) {
                AnimationType animationType = AnimationType.values()[position];
                return new AnimationEditorPageView(context, animationType, delegate);
            }
        }
    }
}
