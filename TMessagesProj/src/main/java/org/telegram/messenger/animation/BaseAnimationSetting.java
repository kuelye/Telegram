package org.telegram.messenger.animation;

import androidx.annotation.StringRes;

import org.telegram.messenger.LocaleController;

public class BaseAnimationSetting {

    private final String titleKey;
    @StringRes private final int titleRes;
    private final ContentType contentType;

    BaseAnimationSetting(String titleKey, @StringRes int titleRes, ContentType contentType) {
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

    public enum ContentType {
        BACKGROUND,
        COLORS,
        INTERPOLATOR
    }
}
