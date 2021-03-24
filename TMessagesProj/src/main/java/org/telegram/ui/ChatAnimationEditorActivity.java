package org.telegram.ui;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

public class ChatAnimationEditorActivity extends BaseFragment {

    private final static int SHARE_PARAMETERS_MENU_ITEM_ID = 0;
    private final static int IMPORT_PARAMETERS_MENU_ITEM_ID = 1;
    private final static int RESTORE_TO_DEFAULT_MENU_ITEM_ID = 2;

    @Override
    public View createView(Context context) {
        // toolbar
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
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
                }
            }
        });

        return new FrameLayout(context);
    }
}
