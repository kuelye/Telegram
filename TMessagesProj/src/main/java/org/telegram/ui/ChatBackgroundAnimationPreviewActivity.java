package org.telegram.ui;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
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
import org.telegram.ui.Components.WallpaperCheckBoxView;
import org.telegram.ui.Editor.BaseAnimationEditorView;

import java.util.ArrayList;
import java.util.List;

public class ChatBackgroundAnimationPreviewActivity extends BaseChatAnimationActivity {

    @Override
    public View createView(Context context) {
        View view = super.createView(context);

        // toolbar
        actionBar.setTitle(LocaleController.getString("BackgroundPreview", R.string.BackgroundPreview));
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
                // root container
                LinearLayout container = new LinearLayout(context);
                container.setOrientation(VERTICAL);
                container.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));

                // background container
                FrameLayout backgroundContainer = new FrameLayout(context);
                container.addView(backgroundContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));

                // animated background
                AnimatedBackgroundView backgroundView = new AnimatedBackgroundView(context);
                backgroundContainer.addView(backgroundView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

                // ANIMATE button
                TextView animateView = new TextView(context);
                animateView.setText(LocaleController.getString("AnimationAnimate", R.string.AnimationAnimate));
                animateView.setGravity(Gravity.CENTER);
                animateView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                animateView.setAllCaps(true);
                animateView.setTextColor(Theme.getColor(Theme.key_dialogButton));
                animateView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                animateView.setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(Theme.getColor(Theme.key_dialogButton)));
                BaseAnimation animation = AnimationController.getBackgroundAnimation();
                animateView.setOnClickListener(v -> backgroundView.animate(animation.getInterpolator(position)));
                container.addView(animateView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));

                // small buttons setup
                int textsCount = 1;
                String[] texts = new String[textsCount];
                int[] textSizes = new int[textsCount];
                WallpaperCheckBoxView[] checkBoxViews = new WallpaperCheckBoxView[textsCount];
                texts[0] = "(DEV) Points";
                int maxTextSize = 0;
                TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setTextSize(AndroidUtilities.dp(14));
                textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                for (int a = 0; a < texts.length; a++) {
                    textSizes[a] = (int) Math.ceil(textPaint.measureText(texts[a]));
                    maxTextSize = Math.max(maxTextSize, textSizes[a]);
                }

                // small buttons container
                LinearLayout buttonsContainer = new LinearLayout(context);
                buttonsContainer.setOrientation(HORIZONTAL);
                backgroundContainer.addView(buttonsContainer, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 34, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));

                // small buttons
                for (int i = 0; i < textsCount; i++) {
                    WallpaperCheckBoxView checkBoxView = new WallpaperCheckBoxView(context, true);
                    checkBoxViews[i] = checkBoxView;
                    checkBoxView.setText(texts[i], textSizes[i], maxTextSize);
                    int width = maxTextSize + AndroidUtilities.dp(14 * 2 + 28);
                    buttonsContainer.addView(checkBoxViews[i], LayoutHelper.createLinear(width, LayoutHelper.WRAP_CONTENT, Gravity.NO_GRAVITY, i > 0 ? 8 : 0, 0, 0, 0));
                    int finalI = i;
                    checkBoxView.setOnClickListener(v -> {
                        if (finalI == 0) {
                            backgroundView.setDevPointsVisible(!backgroundView.isDevPointsVisible());
                            checkBoxView.setChecked(backgroundView.isDevPointsVisible(), true);
                        }
                    });
                }

                return container;
            }
        }
    }
}
