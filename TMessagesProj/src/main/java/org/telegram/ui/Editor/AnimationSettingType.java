package org.telegram.ui.Editor;

import androidx.annotation.StringRes;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

enum AnimationSettingType {

    BACKGROUND_PREVIEW("AnimationBackgroundPreview", R.string.AnimationBackgroundPreview, ContentType.BACKGROUND),
    COLORS("AnimationColors", R.string.AnimationColors, ContentType.COLORS),
    SEND_MESSAGE("AnimationSendMessage", R.string.AnimationSendMessage, ContentType.INTERPOLATOR),
    OPEN_CHAT("AnimationOpenChat", R.string.AnimationOpenChat, ContentType.INTERPOLATOR),
    JUMP_TO_MESSAGE("AnimationJumpToMessage", R.string.AnimationJumpToMessage, ContentType.INTERPOLATOR);

    private final String titleKey;
    @StringRes private final int titleRes;
    private final ContentType contentType;

    AnimationSettingType(String titleKey, @StringRes int titleRes, ContentType contentType) {
        this.titleKey = titleKey;
        this.titleRes = titleRes;
        this.contentType = contentType;
    }

    public String getTitle() {
        return LocaleController.getString(titleKey, titleRes);
    }

    public ContentType getContentType() {
        return contentType;
    }

    enum ContentType {
        BACKGROUND,
        COLORS,
        INTERPOLATOR
    }
}
