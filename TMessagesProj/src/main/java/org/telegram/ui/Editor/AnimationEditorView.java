package org.telegram.ui.Editor;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.LayoutHelper;

public class AnimationEditorView extends LinearLayout {

    private final FilterTabsView tabsView;

    public AnimationEditorView(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);

        // tabs
        tabsView = new FilterTabsView(context) {

        };
        tabsView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));
        tabsView.setDelegate(new FilterTabsView.FilterTabsViewDelegate() {
            @Override
            public void onPageSelected(int page, boolean forward) {

            }

            @Override
            public void onPageScrolled(float progress) {

            }

            @Override
            public void onSamePageSelected() {

            }

            @Override
            public int getTabCounter(int tabId) {
                return 0;
            }

            @Override
            public boolean didSelectTab(FilterTabsView.TabView tabView, boolean selected) {
                return false;
            }

            @Override
            public boolean isTabMenuVisible() {
                return false;
            }

            @Override
            public void onDeletePressed(int id) {

            }

            @Override
            public void onPageReorder(int fromId, int toId) {

            }

            @Override
            public boolean canPerformActions() {
                return true;
            }
        });
        addView(tabsView);
        tabsView.setIsFirstTabAll(false);
        for (int i = 0, l = AnimationType.values().length; i < l; ++i) {
            tabsView.addTab(i, i, AnimationType.values()[i].getTitle());
        }
        tabsView.finishAddingTabs(true);
        tabsView.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
    }

    public FilterTabsView getTabsView() {
        return tabsView;
    }
}
