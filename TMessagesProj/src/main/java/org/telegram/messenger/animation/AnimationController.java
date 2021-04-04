package org.telegram.messenger.animation;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationController {

    private static volatile AnimationController instance = null;

    private final Map<AnimationType, BaseAnimation> animations = new HashMap<>();
    private final Map<AnimationType, List<OnAnimationChangedListener>> listeners = new HashMap<>();

    public static AnimationController getInstance() {
        AnimationController localInstance = instance;
        if (localInstance == null) {
            synchronized (AnimationController.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new AnimationController();
                }
            }
        }
        return localInstance;
    }

    private AnimationController() {
        animations.put(AnimationType.BACKGROUND, new BackgroundAnimation());
        animations.put(AnimationType.SHORT_TEXT, new DefaultAnimation(AnimationType.SHORT_TEXT));
        animations.put(AnimationType.LONG_TEXT, new DefaultAnimation(AnimationType.LONG_TEXT));
        animations.put(AnimationType.LINK, new DefaultAnimation(AnimationType.LINK));
        animations.put(AnimationType.EMOJI, new ImageAnimation(AnimationType.EMOJI));
        animations.put(AnimationType.PHOTO, new ImageAnimation(AnimationType.PHOTO));
        animations.put(AnimationType.STICKER, new ImageAnimation(AnimationType.STICKER));
        animations.put(AnimationType.VOICE, new DefaultAnimation(AnimationType.VOICE));
        animations.put(AnimationType.VIDEO, new DefaultAnimation(AnimationType.VIDEO));

        String savedAnimations = MessagesController.getGlobalMainSettings().getString("animations", null);
        if (savedAnimations == null) {
            restoreToDefaultInternal();
        } else {
            try {
                JSONObject jsonObject = new JSONObject(savedAnimations);
                for (BaseAnimation animation : animations.values()) {
                    if (jsonObject.has(animation.getAnimationType().getJsonKey())) {
                        animation.applyJson(jsonObject.getJSONObject(animation.getAnimationType().getJsonKey()));
                    }
                }
            } catch (JSONException e) {
                FileLog.e(e);
            }
        }
    }

    public static BaseAnimation getAnimation(AnimationType type) {
        return getInstance().animations.get(type);
    }

    public static BackgroundAnimation getBackgroundAnimation() {
        return (BackgroundAnimation) getAnimation(AnimationType.BACKGROUND);
    }

    public static void addOnAnimationChangedListener(AnimationType animationType, OnAnimationChangedListener listener) {
        List<OnAnimationChangedListener> listeners = getInstance().listeners.get(animationType);
        if (listeners == null) {
            listeners = new ArrayList<>();
            getInstance().listeners.put(animationType, listeners);
        }
        listeners.add(listener);
    }

    public static void removeOnAnimationChangedListener(AnimationType animationType, OnAnimationChangedListener listener) {
        List<OnAnimationChangedListener> listeners = getInstance().listeners.get(animationType);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public static void emitAnimationChange(AnimationType animationType) {
        List<OnAnimationChangedListener> listeners;
        if (animationType == null) {
            for (AnimationType type : getInstance().listeners.keySet()) {
                listeners = getInstance().listeners.get(type);
                if (listeners != null) {
                    for (OnAnimationChangedListener listener : listeners) {
                        listener.onAnimationChanged(animationType);
                    }
                }
            }
        } else {
            listeners = getInstance().listeners.get(animationType);
            if (listeners != null) {
                for (OnAnimationChangedListener listener : listeners) {
                    listener.onAnimationChanged(animationType);
                }
            }
        }
    }

    public static void save() {
        JSONObject jsonObject = getInstance().toJson();
        String savedAnimations = jsonObject.toString();
//        Log.v("AnimationController", "GUB save: savedAnimations=" + savedAnimations);
        MessagesController.getGlobalMainSettings().edit().putString("animations", savedAnimations).apply();
    }

    public static void restoreToDefault() {
        getInstance().restoreToDefaultInternal();
    }

    private JSONObject toJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            for (BaseAnimation animation : animations.values()) {
                jsonObject.put(animation.getAnimationType().getJsonKey(), animation.toJson());
            }
            return jsonObject;
        } catch (JSONException e) {
            FileLog.e(e);
        }
        return null;
    }

    private void restoreToDefaultInternal() {
        for (BaseAnimation animation : getInstance().animations.values()) {
            animation.restoreToDefault();
        }
        emitAnimationChange(null);
    }

    public interface OnAnimationChangedListener {
        void onAnimationChanged(AnimationType animationType);
    }
}
