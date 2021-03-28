package org.telegram.messenger.animation;

import androidx.annotation.StringRes;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public enum AnimationType {

    BACKGROUND("AnimationBackground", R.string.AnimationBackground),
    SHORT_TEXT("AnimationShortText", R.string.AnimationShortText),
    LONG_TEXT("AnimationLongText", R.string.AnimationLongText),
    LINK("AnimationLink", R.string.AnimationLink),
    EMOJI("AnimationEmoji", R.string.AnimationEmoji),
    PHOTO("AnimationPhoto", R.string.AnimationPhoto),
    STICKER("AnimationSticker", R.string.AnimationSticker),
    VOICE("AnimationVoice", R.string.AnimationVoice),
    VIDEO("AnimationVideo", R.string.AnimationVideo);

    private final String titleKey;
    @StringRes private final int titleRes;

    private AnimationType(String titleKey, @StringRes int titleRes) {
        this.titleKey = titleKey;
        this.titleRes = titleRes;
    }

    public String getTitle() {
        return LocaleController.getString(titleKey, titleRes);
    }
}