package org.telegram.messenger.animation;

import androidx.annotation.StringRes;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public enum AnimationType {

    BACKGROUND("AnimationBackground", R.string.AnimationBackground, "background"),
    SHORT_TEXT("AnimationShortText", R.string.AnimationShortText, "shortText"),
    LONG_TEXT("AnimationLongText", R.string.AnimationLongText, "longText"),
    LINK("AnimationLink", R.string.AnimationLink, "link"),
    EMOJI("AnimationEmoji", R.string.AnimationEmoji, "emoji"),
    PHOTO("AnimationPhoto", R.string.AnimationPhoto, "photo"),
    STICKER("AnimationSticker", R.string.AnimationSticker, "sticker"),
    VOICE("AnimationVoice", R.string.AnimationVoice, "voice"),
    VIDEO("AnimationVideo", R.string.AnimationVideo, "video");

    private final String titleKey;
    @StringRes private final int titleRes;
    private final String jsonKey;

    private AnimationType(String titleKey, @StringRes int titleRes, String jsonKey) {
        this.titleKey = titleKey;
        this.titleRes = titleRes;
        this.jsonKey = jsonKey;
    }

    public String getTitle() {
        return LocaleController.getString(titleKey, titleRes);
    }

    public String getJsonKey() {
        return jsonKey;
    }
}