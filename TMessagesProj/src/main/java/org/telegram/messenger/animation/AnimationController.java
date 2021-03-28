package org.telegram.messenger.animation;

import android.view.animation.Animation;

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
        List<OnAnimationChangedListener> listeners = getInstance().listeners.get(animationType);
        if (listeners != null) {
            for (OnAnimationChangedListener listener : listeners) {
                listener.onAnimationChanged();
            }
        }
    }

    public interface OnAnimationChangedListener {
        void onAnimationChanged();
    }
}
