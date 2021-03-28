package org.telegram.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.animation.AnimationController;
import org.telegram.messenger.animation.BaseAnimation;
import org.telegram.messenger.animation.BaseAnimationSetting;
import org.telegram.messenger.animation.InterpolatorAnimationSetting;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedBackgroundView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Editor.BaseAnimationEditorView;

import java.util.ArrayList;
import java.util.List;

public class ChatBackgroundAnimationPreviewActivity extends BaseChatAnimationActivity {

    @Override
    public View createView(Context context) {
        View view = super.createView(context);

        // toolbar
        actionBar.setTitle(LocaleController.getString("AnimationBackgroundPreview", R.string.AnimationBackgroundPreview));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                if (id == id) {
                    finishFragment();
                }
            }
        });

        return view;
    }

    @Override
    protected BaseAnimationEditorView createEditorView() {
        return new BackgroundAnimationPreviewView(getParentActivity());
    }

    private static class BackgroundAnimationPreviewView extends BaseAnimationEditorView {

        private final List<BaseAnimationSetting> settings = new ArrayList<>();

        public BackgroundAnimationPreviewView(Context context) {
            super(context);
            BaseAnimation animation = AnimationController.getBackgroundAnimation();
            for (BaseAnimationSetting setting : animation.getSettings()) {
                if (setting instanceof InterpolatorAnimationSetting) {
                    this.settings.add(setting);
                }
            }
            initializeTabs();
            viewPagerAdapter.setCount(getTabsCount());
            viewPagerAdapter.notifyDataSetChanged();
        }

        @Override
        protected BaseAdapter createAdapter() {
            return new BackgroundAnimationPreviewView.Adapter(getContext(), getTabsCount());
        }

        @Override
        protected int getTabsCount() {
            return settings == null ? 0 : settings.size();
        }

        @Override
        protected String getTabTitle(int position) {
            return settings.get(position).getTitle();
        }

        private static class Adapter extends BaseAdapter {

            Adapter(Context context, int count) {
                super(context, count);
            }

            @Override
            protected View instantiateView(int position) {
                LinearLayout container = new LinearLayout(context);
                container.setOrientation(VERTICAL);
                container.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
                AnimatedBackgroundView backgroundView = new AnimatedBackgroundView(context);
                container.addView(backgroundView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));
                TextView view = new TextView(context);
                view.setText(LocaleController.getString("AnimationAnimate", R.string.AnimationAnimate));
                view.setGravity(Gravity.CENTER);
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                view.setAllCaps(true);
                view.setTextColor(Theme.getColor(Theme.key_dialogButton));
                view.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                view.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(Theme.getColor(Theme.key_dialogButton)));
                BaseAnimation animation = AnimationController.getBackgroundAnimation();
                view.setOnClickListener(v -> backgroundView.animate(animation.getInterpolator(position)));
                container.addView(view, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));
                return container;
            }
        }
    }
}
