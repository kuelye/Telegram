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
import org.telegram.messenger.animation.Interpolator;
import org.telegram.messenger.animation.InterpolatorAnimationSetting;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextColorPickerCell;
import org.telegram.ui.Cells.TextSpinnerCell;
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
                    for (int id = 0; id < BackgroundAnimation.POINTS_COUNT; ++id) {
                        final int finalId = id;
                        items.add(new TextColorItem(id + 1, colorsSetting.getColor(animation, id), color -> {
                            colorsSetting.setColor(animation, finalId, color);
                        }));
                    }
                    break;
                case INTERPOLATOR:
                    InterpolatorAnimationSetting interpolatorSetting = (InterpolatorAnimationSetting) setting;
                    Interpolator interpolator = animation.getInterpolator(interpolatorSetting.getInterpolatorId());
                    TextSpinnerCell.Item[] durationItems = new TextSpinnerCell.Item[Interpolator.DURATIONS.length];
                    TextSpinnerCell.Item selectedItem = null;
                    for (int j = 0, c = Interpolator.DURATIONS.length; j < c; ++j) {
                        int duration = Interpolator.DURATIONS[j];
                        TextSpinnerCell.Item item = new TextSpinnerCell.Item(duration, LocaleController.formatString("AnimationDurationTemplate", R.string.AnimationDurationTemplate, duration));
                        if (duration == interpolator.getDuration()) {
                            selectedItem = item;
                        }
                        durationItems[j] = item;
                    }
                    InterpolatorItem interpolatorItem = new InterpolatorItem(interpolator.getDuration());
                    int interpolatorItemI = i + 1;
                    items.add(new TextSpinnerItem(LocaleController.getString("AnimationDuration", R.string.AnimationDuration), durationItems, selectedItem, item -> {
                        interpolator.setDuration(item.getValue());
                        interpolatorItem.setDuration(item.getValue());
                        adapter.notifyItemChanged(interpolatorItemI);
                    }));
                    items.add(interpolatorItem);
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

        final static int HEADER = 0;
        final static int SHADOW = 1;
        final static int TEXT = 2;
        final static int TEXT_SPINNER = 3;
        final static int BACKGROUND = 4;
        final static int TEXT_COLOR_PICKER = 5;
        final static int INTERPOLATOR = 6;

        private final int viewType;
        private boolean isEnabled;

        BaseItem(int viewType) {
            this.viewType = viewType;
        }

        static View create(Context context, int viewType) {
            View view;
            switch (viewType) {
                case HEADER:
                    view = new HeaderCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case SHADOW:
                    view = new ShadowSectionCell(context);
                    break;
                case TEXT:
                    view = new TextCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TEXT_SPINNER:
                    view = new TextSpinnerCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case BACKGROUND:
                    view = new AnimationBackgroundCell(context);
                    break;
                case TEXT_COLOR_PICKER:
                    view = new TextColorPickerCell(context);
                    break;
                case INTERPOLATOR:
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
            super(HEADER);
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
            super(SHADOW);
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
            super(TEXT);
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
        private final TextSpinnerCell.Item[] items;
        private TextSpinnerCell.Item selectedItem;
        private final TextSpinnerCell.OnItemSelectedListener listener;

        TextSpinnerItem(String title, TextSpinnerCell.Item[] items, TextSpinnerCell.Item selectedItem, TextSpinnerCell.OnItemSelectedListener listener) {
            super(TEXT_SPINNER);
            this.title = title;
            this.items = items;
            this.selectedItem = selectedItem;
            this.listener = listener;
            setEnabled(true);
        }

        @Override
        void bind(Context context, View view) {
            TextSpinnerCell cell = (TextSpinnerCell) view;
            cell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            cell.setText(title, true);
            cell.swapItems(items);
            cell.setSelectedItem(selectedItem);
            cell.setOnItemSelectedListener(item -> {
                selectedItem = item;
                listener.onItemSelected(item);
            });
        }

        @Override
        void click(View view) {
            TextSpinnerCell cell = (TextSpinnerCell) view;
            cell.showPopup();
        }
    }

    private static class BackgroundItem extends BaseItem {

        BackgroundItem() {
            super(BACKGROUND);
        }

        @Override
        void bind(Context context, View view) {
            // TODO
        }
    }

    private static class TextColorItem extends BaseItem {

        private final int id;
        @ColorInt private int color;
        private final TextColorPickerCell.OnColorAppliedListener listener;

        TextColorItem(int id, int color, TextColorPickerCell.OnColorAppliedListener listener) {
            super(TEXT_COLOR_PICKER);
            this.id = id;
            this.color = color;
            this.listener = listener;
            setEnabled(true);
        }

        @Override
        void bind(Context context, View view) {
            TextColorPickerCell cell = (TextColorPickerCell) view;
            cell.setColor(color);
            cell.setText(String.format(LocaleController.getString("AnimationColor", R.string.AnimationColor), id), true);
            cell.setOnColorAppliedListener(listener);
        }

        @Override
        void click(View view) {
            TextColorPickerCell cell = (TextColorPickerCell) view;
            cell.showPicker();
        }
    }

    private static class InterpolatorItem extends BaseItem {

        private int duration;

        InterpolatorItem(int duration) {
            super(INTERPOLATOR);
            this.duration = duration;
        }

        @Override
        void bind(Context context, View view) {
            AnimationInterpolatorCell cell = (AnimationInterpolatorCell) view;
            cell.setDuration(duration);
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }
    }

    public interface Delegate {
        void onBackgroundPreviewCalled();
    }
}
