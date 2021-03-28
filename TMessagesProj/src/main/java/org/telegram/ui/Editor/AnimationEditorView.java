package org.telegram.ui.Editor;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.animation.AnimationType;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.LayoutHelper;

import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;

public class AnimationEditorView extends LinearLayout {

    private final FilterTabsView tabsView;
    private final ViewPager viewPager;
    private final Adapter viewPagerAdapter;

    public AnimationEditorView(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        // tabs
        tabsView = new FilterTabsView(context);
        tabsView.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));
        tabsView.setDelegate(new FilterTabsView.FilterTabsViewDelegate() {
            @Override
            public void onPageSelected(int page, boolean forward) {
                viewPager.setCurrentItem(page);
            }

            @Override
            public void onPageScrolled(float progress) {
                // stub
            }

            @Override
            public void onSamePageSelected() {
                // TODO [CONTEST] scroll to top
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
                // stub
            }

            @Override
            public void onPageReorder(int fromId, int toId) {
                // stub
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

        // pager
        viewPager = new ViewPager(context);
        viewPager.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1, Gravity.NO_GRAVITY));
        viewPagerAdapter = new Adapter(context);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private int previousState = -1;
            private int previousPosition = -1;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset != 0) {
                    if (previousPosition <= position) {
                        position += 1;
                    } else {
                        positionOffset = 1 - positionOffset;
                    }
                    tabsView.selectTabWithId(position, positionOffset);
                }
            }

            @Override
            public void onPageSelected(int position) {
                // stub
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (previousState != SCROLL_STATE_IDLE && (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_DRAGGING)) {
                    tabsView.selectTabWithId(viewPager.getCurrentItem(), 1);
                    previousPosition = viewPager.getCurrentItem();
                }
                previousState = state;
            }
        });
        addView(viewPager);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        viewPagerAdapter.onDetached();
    }

    private static class Adapter extends PagerAdapter {

        private final Context context;
        private final View[] views;

        Adapter(Context context) {
            this.context = context;
            views = new View[getCount()];
        }

        @Override
        public int getCount() {
            return AnimationType.values().length;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = views[position];
            if (view == null) {
                AnimationType animationType = AnimationType.values()[position];
                view = new AnimationEditorPageView(context, animationType);
                views[position] = view;
            }
            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        public void onDetached() {
            for (int i = 0, l = views.length; i < l; i++) {
                views[i] = null;
            }
        }
    }
}
