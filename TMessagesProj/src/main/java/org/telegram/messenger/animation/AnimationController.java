package org.telegram.messenger.animation;

import android.view.animation.Animation;

import java.util.HashMap;
import java.util.Map;

public class AnimationController {

    private static volatile AnimationController instance = null;

    private final Map<AnimationType, BaseAnimation> animations = new HashMap<>();

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
}
