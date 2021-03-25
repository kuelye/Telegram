package org.telegram.ui.Editor;

import android.content.Context;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextSettingsCell;

public class AnimationTextColorCell extends TextSettingsCell {

    public AnimationTextColorCell(Context context) {
        super(context);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
    }
}
