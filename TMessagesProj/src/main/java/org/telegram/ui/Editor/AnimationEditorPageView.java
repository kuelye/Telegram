package org.telegram.ui.Editor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.animation.AnimationController;
import org.telegram.messenger.animation.AnimationType;
import org.telegram.messenger.animation.BackgroundAnimation;
import org.telegram.messenger.animation.BaseAnimation;
import org.telegram.messenger.animation.BaseAnimationSetting;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

public class AnimationEditorPageView extends RecyclerListView {

    private final List<BaseItem> items = new ArrayList<>();

    public AnimationEditorPageView(@NonNull Context context, AnimationType animationType, Delegate delegate) {
        super(context);

        // setup RecyclerListView
        setVerticalScrollBarEnabled(false);
        setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        Adapter adapter = new Adapter();
        setAdapter(adapter);
        setOnItemClickListener((view, position, x, y) -> {
            getItem(position).click(view);
        });

        // add items
        BaseAnimation animation = AnimationController.getAnimation(animationType);
        if (animation == null) return;
        BaseAnimationSetting[] settings = animation.getSettings();
        for (int i = 0, l = settings.length; i < l; ++i) {
            BaseAnimationSetting setting = settings[i];
            items.add(new HeaderItem(setting.getTitle()));
            switch (setting.getContentType()) {
                case BACKGROUND:
                    items.add(new BackgroundItem());
                    items.add(new TextItem(LocaleController.getString("AnimationOpenFullScreen", R.string.AnimationOpenFullScreen), view -> delegate.onBackgroundPreviewCalled()));
                    break;
                case COLORS:
                    BackgroundAnimation.BackgroundColorsAnimationSetting colorsSetting = (BackgroundAnimation.BackgroundColorsAnimationSetting) setting;
                    for (int id = 0; id < BackgroundAnimation.COLORS_COUNT; ++id) {
                        final int finalId = id;
                        items.add(new TextColorItem(id + 1, colorsSetting.getColor(animation, id), color -> {
                            colorsSetting.setColor(animation, finalId, color);
                        }));
                    }
                    break;
                case INTERPOLATOR:
                    items.add(new TextSpinnerItem(LocaleController.getString("AnimationDuration", R.string.AnimationDuration), "1000ms"));
                    items.add(new InterpolatorItem());
                    break;
            }
            items.add(new ShadowItem(i == l - 1));
        }
        adapter.notifyDataSetChanged();
    }

    private BaseItem getItem(int position) {
        return items.get(position);
    }

    private class Adapter extends RecyclerListView.SelectionAdapter {

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getViewType();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = BaseItem.create(getContext(), viewType);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            getItem(position).bind(getContext(), holder.itemView);
        }

        @Override
        public boolean isEnabled(ViewHolder holder) {
            return getItem(holder.getAdapterPosition()).isEnabled();
        }
    }

    private static abstract class BaseItem {

        final static int HEADER_CELL = 0;
        final static int SHADOW_CELL = 1;
        final static int TEXT_CELL = 2;
        final static int TEXT_SPINNER_CELL = 3;
        final static int BACKGROUND_CELL = 4;
        final static int TEXT_COLOR_CELL = 5;
        final static int INTERPOLATOR_CELL = 6;

        private final int viewType;
        private boolean isEnabled;

        BaseItem(int viewType) {
            this.viewType = viewType;
        }

        static View create(Context context, int viewType) {
            View view;
            switch (viewType) {
                case HEADER_CELL:
                    view = new HeaderCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case SHADOW_CELL:
                    view = new ShadowSectionCell(context);
                    break;
                case TEXT_CELL:
                    view = new TextCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TEXT_SPINNER_CELL:
                    view = new TextSettingsCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case BACKGROUND_CELL:
                    view = new AnimationBackgroundCell(context);
                    break;
                case TEXT_COLOR_CELL:
                    view = new AnimationTextColorCell(context);
                    break;
                case INTERPOLATOR_CELL:
                    view = new AnimationInterpolatorCell(context);
                    break;
                default:
                    throw new IllegalArgumentException("Not implemented for viewType=" + viewType);
            }
            return view;
        }

        abstract void bind(Context context, View view);
        void click(View view) {}

        int getViewType() {
            return viewType;
        }

        void setEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        boolean isEnabled() {
            return isEnabled;
        }
    }

    private static class HeaderItem extends BaseItem {

        private final String title;

        HeaderItem(String title) {
            super(HEADER_CELL);
            this.title = title;
        }

        @Override
        void bind(Context context, View view) {
            ((HeaderCell) view).setText(title);
        }
    }

    private static class ShadowItem extends BaseItem {

        private final boolean isBottom;

        ShadowItem(boolean isBottom) {
            super(SHADOW_CELL);
            this.isBottom = isBottom;
        }

        @Override
        void bind(Context context, View view) {
            if (isBottom) {
                view.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
            } else {
                view.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
            }
        }
    }

    private static class TextItem extends BaseItem {

        private final String title;
        private final OnClickListener listener;

        TextItem(String title, OnClickListener listener) {
            super(TEXT_CELL);
            this.title = title;
            this.listener = listener;

            if (listener != null) {
                setEnabled(true);
            }
        }

        @Override
        void bind(Context context, View view) {
            TextCell cell = (TextCell) view;
            cell.setText(title, false);
            cell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
        }

        @Override
        void click(View view) {
            listener.onClick(view);
        }
    }

    private static class TextSpinnerItem extends BaseItem {

        private final String title;
        private final String value;

        TextSpinnerItem(String title, String value) {
            super(TEXT_SPINNER_CELL);
            this.title = title;
            this.value = value;
        }

        @Override
        void bind(Context context, View view) {
            TextSettingsCell cell = (TextSettingsCell) view;
            cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            cell.setTextAndValue(title, value, true);
        }
    }

    private static class BackgroundItem extends BaseItem {

        BackgroundItem() {
            super(BACKGROUND_CELL);
        }

        @Override
        void bind(Context context, View view) {
            // TODO
        }
    }

    private static class TextColorItem extends BaseItem {

        private final int id;
        @ColorInt private int color;
        private final AnimationTextColorCell.ApplyDelegate delegate;

        TextColorItem(int id, int color, AnimationTextColorCell.ApplyDelegate delegate) {
            super(TEXT_COLOR_CELL);
            this.id = id;
            this.color = color;
            this.delegate = delegate;
            setEnabled(true);
        }

        @Override
        void bind(Context context, View view) {
            AnimationTextColorCell cell = (AnimationTextColorCell) view;
            cell.setColor(color);
            cell.setText(String.format(LocaleController.getString("AnimationColor", R.string.AnimationColor), id), true);
            cell.setDelegate(delegate);
        }

        @Override
        void click(View view) {
            AnimationTextColorCell cell = (AnimationTextColorCell) view;
            cell.showPicker();
        }
    }

    private static class InterpolatorItem extends BaseItem {

        InterpolatorItem() {
            super(INTERPOLATOR_CELL);
        }

        @Override
        void bind(Context context, View view) {
            // TODO
        }
    }

    public interface Delegate {
        void onBackgroundPreviewCalled();
    }
}
