package org.telegram.ui.Editor;

import androidx.annotation.StringRes;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public enum AnimationType {

    BACKGROUND("AnimationBackground", R.string.AnimationBackground, Defaults.BACKGROUND_SETTING_TYPES),
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
    private final AnimationSettingType[] settingTypes;

    AnimationType(String titleKey, @StringRes int titleRes) {
        this(titleKey, titleRes, Defaults.DEFAULT_SETTING_TYPES);
    }

    AnimationType(String titleKey, @StringRes int titleRes, AnimationSettingType[] settingTypes) {
        this.titleKey = titleKey;
        this.titleRes = titleRes;
        this.settingTypes = settingTypes;
    }

    String getTitle() {
        return LocaleController.getString(titleKey, titleRes);
    }

    AnimationSettingType[] getSettingTypes() {
        return settingTypes;
    }

    private static class Defaults {
        private final static AnimationSettingType[] BACKGROUND_SETTING_TYPES = {
                AnimationSettingType.BACKGROUND_PREVIEW, AnimationSettingType.COLORS,
                AnimationSettingType.SEND_MESSAGE, AnimationSettingType.OPEN_CHAT,
                AnimationSettingType.JUMP_TO_MESSAGE };

        private final static AnimationSettingType[] DEFAULT_SETTING_TYPES = {
                AnimationSettingType.SEND_MESSAGE };
    }
}