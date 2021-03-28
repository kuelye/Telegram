package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextSpinnerCell extends TextSettingsCell {

    private ActionBarPopupWindow popupWindow;

    private final List<Item> items = new ArrayList<>();
    private Item selectedItem = null;

    private OnItemSelectedListener onItemSelectedListener;

    public TextSpinnerCell(Context context) {
        super(context);
    }

    public void swapItems(Item[] items) {
        this.items.clear();
        Collections.addAll(this.items, items);
    }

    public void setSelectedItem(Item item) {
        setSelectedItem(item, false);
    }

    private void setSelectedItem(Item item, boolean internal) {
        selectedItem = item;
        setValue(selectedItem.title);
        if (internal && onItemSelectedListener != null) {
            onItemSelectedListener.onItemSelected(item);
        }
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        onItemSelectedListener = listener;
    }

    public void showPopup() {
        // dismiss previous popup
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }

        // popup layout
        Rect rect = new Rect();
        ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext());
        popupLayout.setOnTouchListener(new View.OnTouchListener() {

            private final int[] pos = new int[2];

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        View contentView = popupWindow.getContentView();
                        contentView.getLocationInWindow(pos);
                        rect.set(pos[0], pos[1], pos[0] + contentView.getMeasuredWidth(), pos[1] + contentView.getMeasuredHeight());
                        if (!rect.contains((int) event.getX(), (int) event.getY())) {
                            popupWindow.dismiss();
                        }
                    }
                } else if (event.getActionMasked() == MotionEvent.ACTION_OUTSIDE) {
                    if (popupWindow != null && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                }
                return false;
            }
        });
        popupLayout.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        });
        Rect backgroundPaddings = new Rect();
        Drawable shadowDrawable = getContext().getResources().getDrawable(R.drawable.popup_fixed_alert).mutate();
        shadowDrawable.getPadding(backgroundPaddings);
        popupLayout.setBackgroundDrawable(shadowDrawable);
        popupLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));

        // content
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0, l = items.size(); i < l; ++i) {
            Item item = items.get(i);
            ActionBarMenuSubItem cell = new ActionBarMenuSubItem(getContext(), i == 0, i == l - 1);
            cell.setText(item.title);
            cell.setOnClickListener(v -> {
                setSelectedItem(item, true);
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
            });
            linearLayout.addView(cell);
        }
        linearLayout.setMinimumHeight(AndroidUtilities.dp(200));
        popupLayout.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        // popup window
        popupWindow = new ActionBarPopupWindow(popupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT) {
            @Override
            public void dismiss() {
                super.dismiss();
                if (popupWindow != this) return;
                popupWindow = null;
            }
        };
        popupWindow.setDismissAnimationDuration(220);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setClippingEnabled(true);
        popupWindow.setAnimationStyle(R.style.PopupContextAnimation);
        popupWindow.setFocusable(true);
        popupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
        popupWindow.getContentView().setFocusableInTouchMode(true);
        popupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
        int[] location = new int[2];
        valueTextView.getLocationOnScreen(location);
        int popupX = location[0] + backgroundPaddings.left - AndroidUtilities.dp(16);
        int popupY = location[1];
        popupWindow.showAtLocation((View) getParent(), Gravity.LEFT | Gravity.TOP, popupX, popupY);
    }

    public static class Item {

        private final int value;
        private final String title;

        public Item(int value, String title) {
            this.value = value;
            this.title = title;
        }

        public int getValue() {
            return value;
        }

        public String getTitle() {
            return title;
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Item item);
    }
}
