package org.telegram.ui.Cells;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.animation.AnimationType;
import org.telegram.messenger.animation.BaseChatAnimation;
import org.telegram.messenger.animation.EmojiAnimation;
import org.telegram.messenger.animation.Interpolator;
import org.telegram.messenger.animation.TextAnimation;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.EditTextCaption;
import org.telegram.ui.Components.RecyclerListView;

public class AnimatedChatMessageCell extends ChatMessageCell {

    private final static int X = 0;
    private final static int Y = 1;
    private final static int CORRECTED_Y = 2;
    private final static int COLOR_BACKGROUND = 3;
    private final static int BUBBLE_BACKGROUND_WIDTH = 4;
    private final static int BUBBLE_BACKGROUND_RIGHT_OFFSET = 5;
    private final static int TEXT_SIZE = 6;
    private final static int TIME_ALPHA = 7;
    private final static int EMOJI_PHOTO_COORDS = 8;

    private final BaseChatAnimation globalAnimation;
    private final int duration;
    private final Delegate delegate;

    private ChatMessageCell realCell;
    private ValueAnimator animator;
    private ValueAnimator correctionAnimator;

    private final int startEnterViewHeight;
    private int editHeightDelta;

    private final SparseArray<Object[]> parameters = new SparseArray<>();
    private final float[] rect = new float[4];

    private boolean isRealCellLayoutDone = false;
    private boolean isAnimationCorrected = true;
    private boolean isAnimationStarted = true;
    private boolean isAnimationFinished = false;
    private boolean isCorrectionAnimationStarted = false;
    private boolean isCorrectionAnimationFinished = false;
    private boolean isAnimationDone = false;

    public AnimatedChatMessageCell(Context context, MessageObject obj, BaseChatAnimation animation, int duration, Delegate delegate) {
        super(context);
        globalAnimation = animation;
        this.duration = duration;
        this.delegate = delegate;

        isAnimated = true;

        setMessageObject(obj, getCurrentMessagesGroup(), isPinnedBottom(), isPinnedTop());
        setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
            @Override
            public TextSelectionHelper.ChatListTextSelectionHelper getTextSelectionHelper() {
                return delegate.getTextSelectionHelper();
            }
        });

        startEnterViewHeight = delegate.getChatActivityEnterView().getHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        checkAnimationStart();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        checkAnimationStart();
    }

    public void setRealCell(ChatMessageCell cell) {
//        Log.v("GUB", "setRealCell: text=" + cell.getMessageObject().messageText + ", isDrawn=" + isDrawn);
        if (realCell != cell) {
            realCell = cell;
            realCell.setHiddenBecauseAnimated(true);
            realCell.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    isRealCellLayoutDone = true;
                    realCell.removeOnLayoutChangeListener(this);
                    checkAnimationStart();
                }
            });
        }
    }

    public void correctAnimation() {
//        Log.v("GUB", "correctAnimation: text=" + getMessageObject().messageText + ", isAnimationCorrected=" + isAnimationCorrected);
        if (!isAnimationCorrected && !isAnimationDone) {
            isAnimationCorrected = true;
            if (correctionAnimator != null) {
                correctionAnimator.cancel();
                correctionAnimator = null;
            }
            startCorrectionAnimation();
        }
    }

    public void checkAnimationFinish() {
//        Log.v("GUB", "checkAnimationFinish: isAnimationFinished=" + isAnimationFinished + ", isCorrectionAnimationStarted=" + isCorrectionAnimationStarted + ", isCorrectionAnimationFinished=" + isCorrectionAnimationFinished + ", getEndY()=" + getEndY() + ", getY()=" + getY());
        if (isAnimationFinished && (!isCorrectionAnimationStarted || isCorrectionAnimationFinished) && getEndY() == getY()) {
            endAnimation();
        }
    }

    public void endAnimation() {
//        Log.v("GUB", "endAnimation: text=" + getMessageObject().messageText + ", isAnimationDone=" + isAnimationDone);
        if (isAnimationDone) {
            return;
        }

        isAnimationDone = true;
        cancelAnimation();
        cancelCorrectionAnimation();
        AndroidUtilities.runOnUIThread(() -> {
            if (realCell != null) {
                realCell.setHiddenBecauseAnimated(false);
            }
            delegate.onAnimationEnd(AnimatedChatMessageCell.this);
        });
    }

    public void dropAnimationCorrected() {
//        Log.v("GUB", "dropAnimationCorrected: text=" + getMessageObject().messageText);
        isAnimationCorrected = false;
    }

    public BaseChatAnimation getChatAnimation() {
        return globalAnimation;
    }

    public int getRemainingDuration() {
        if (animator == null || !isAnimationStarted) {
            return duration;
        }
        if (isAnimationFinished) {
            return 0;
        }
        return (int) (duration * (1 - (float) animator.getAnimatedValue()));
    }

    private void checkAnimationStart() {
        if (animator == null && isDrawn && !isAnimationDone && isRealCellLayoutDone && realCell != null) {
            startAnimation();
        }
    }

    private void startAnimation() {
        // x
        ChatActivityEnterView enterView = delegate.getChatActivityEnterView();
        EditTextCaption editText = enterView.getMessageEditText();
        int[] startEditLocation = new int[2];
        editText.getLocationOnScreen(startEditLocation);
        Interpolator xInterpolator = globalAnimation.getXInterpolator();
        int startX = 0;
        int endX = -startEditLocation[0];
        if  (globalAnimation.getAnimationType() == AnimationType.SHORT_TEXT || globalAnimation.getAnimationType() == AnimationType.LONG_TEXT) {
            endX += textX;
        }

        // y
        Interpolator yInterpolator = globalAnimation.getYInterpolator();
        int[] startRealCellLocation = new int[2];
        realCell.getLocationOnScreen(startRealCellLocation);
        int[] startChatLocation = new int[2];
        delegate.getChatListView().getLocationOnScreen(startChatLocation);
        int[] startOverlayLocation = new int[2];
        delegate.getAnimatedMessagesOverlay().getLocationOnScreen(startOverlayLocation);
        editHeightDelta = startEnterViewHeight - delegate.getChatActivityEnterView().getMeasuredHeight();
        int startY = startEditLocation[1] - startOverlayLocation[1];
        int endY = getEndY();
        if  (globalAnimation.getAnimationType() == AnimationType.SHORT_TEXT || globalAnimation.getAnimationType() == AnimationType.LONG_TEXT) {
            startY += editText.getBaseline() - getBottomBaseline() - editHeightDelta;
        }

        // time
        Interpolator timeInterpolator = globalAnimation.getTimeAppearsInterpolator();
        parameters.put(TIME_ALPHA, new Float[] { 0f, 1f });

        if (globalAnimation.getAnimationType() == AnimationType.SHORT_TEXT || globalAnimation.getAnimationType() == AnimationType.LONG_TEXT) {
            // bubble
            int backgroundDrawableRightOffset = backgroundWidth - backgroundDrawableRight + AndroidUtilities.dp(8);
            parameters.put(BUBBLE_BACKGROUND_WIDTH, new Integer[] {
                realCell.backgroundWidth - startEditLocation[0] + backgroundDrawableLeft + AndroidUtilities.dp(11) + getExtraTextX() + backgroundDrawableRightOffset,
                realCell.backgroundWidth
            });
            parameters.put(BUBBLE_BACKGROUND_RIGHT_OFFSET, new Integer[] {
                backgroundDrawableRightOffset,
                0
            });

            // text
            parameters.put(TEXT_SIZE, new Float[] {
                editText.getTextSize(),
                (float) AndroidUtilities.dp(SharedConfig.fontSize)
            });

            // colors
            int endBackgroundColor = Theme.getColor(Theme.key_chat_outBubble);
            int startBackgroundColor = Theme.getColor(Theme.key_windowBackgroundWhite);
            parameters.put(COLOR_BACKGROUND, new Integer[] { startBackgroundColor, endBackgroundColor });
            backgroundPaint.setColor(startBackgroundColor);
        } else if (globalAnimation.getAnimationType() == AnimationType.EMOJI) {
            Paint paint = editText.getPaint();
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float size = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading + AndroidUtilities.dp(2);
            Float[] startCoords = new Float[] {(float) startEditLocation[0], (float) editText.getBaseline() - size + fontMetrics.descent + AndroidUtilities.dp(1), size, size };
            Float[] endCoords = new Float[] { getPhotoImage().getImageX(), getPhotoImage().getImageY(), getPhotoImage().getImageWidth(), getPhotoImage().getImageHeight() };
            parameters.put(EMOJI_PHOTO_COORDS, new Float[][] { startCoords, endCoords });
            endX += endCoords[0];
            endY += endCoords[3] - startCoords[3];
        }

        // finishing x & y
        parameters.put(X, new Integer[] { startX, endX });
        parameters.put(Y, new Integer[] { startY, endY });
        setY((Integer) parameters.get(Y)[0]);

        // ANIMATION
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            float ratio = (float) animation.getAnimatedValue();
            int additionalOffsetY = 0;

            // time
            timeAlphaFactor = lerpFloat(TIME_ALPHA, timeInterpolator.getInterpolation(ratio));

            int additionalOffsetX = 0;
            if (globalAnimation.getAnimationType() == AnimationType.SHORT_TEXT || globalAnimation.getAnimationType() == AnimationType.LONG_TEXT) {
                // bubble
                Interpolator bubbleInterpolator = ((TextAnimation) globalAnimation).getBubbleShapeInterpolator();
                float bubbleInterpolation = bubbleInterpolator.getInterpolation(ratio);
                backgroundWidth = (int) lerpInt(BUBBLE_BACKGROUND_WIDTH, bubbleInterpolation);
                backgroundDrawableRightOffset = (int) lerpInt(BUBBLE_BACKGROUND_RIGHT_OFFSET, bubbleInterpolation);
                 additionalOffsetX = backgroundWidth - (int) parameters.get(BUBBLE_BACKGROUND_WIDTH)[0] + (int) parameters.get(BUBBLE_BACKGROUND_RIGHT_OFFSET)[0];

                // text
                Interpolator textInterpolator = ((TextAnimation) globalAnimation).getTextScaleInterpolator();
                textSize = lerpFloat(TEXT_SIZE, textInterpolator.getInterpolation(ratio));

                // color
                Interpolator colorInterpolator = ((TextAnimation) globalAnimation).getColorChangeInterpolator();
                backgroundPaint.setColor(blendColor(COLOR_BACKGROUND, colorInterpolator.getInterpolation(ratio)));
            } else if (globalAnimation.getAnimationType() == AnimationType.EMOJI) {
                Interpolator emojiInterpolator = ((EmojiAnimation) globalAnimation).getEmojiScaleInterpolator();
                lerpFloats(EMOJI_PHOTO_COORDS, emojiInterpolator.getInterpolation(ratio), rect);
                additionalOffsetX = (int) (((Float[][]) parameters.get(EMOJI_PHOTO_COORDS))[0][0] - rect[0]);
                additionalOffsetY = (int) (((Float[][]) parameters.get(EMOJI_PHOTO_COORDS))[0][3] - rect[3]);
                getPhotoImage().setImageCoords(rect[0], rect[1], rect[2], rect[3]);
            }

            // x
            setAnimationOffsetX(additionalOffsetX + lerpInt(X, xInterpolator.getInterpolation(ratio)));

            // y
            if (!isCorrectionAnimationStarted) {
                int editCompensation = 0;
                if (editHeightDelta > 0) {
                    int[] overlayLocation = new int[2];
                    delegate.getAnimatedMessagesOverlay().getLocationOnScreen(overlayLocation);
                    editCompensation = overlayLocation[1] - startOverlayLocation[1];
                    if (editCompensation == editHeightDelta) {
                        editHeightDelta = 0;
                        editCompensation = 0;
                    }
                }
                setY(additionalOffsetY + lerpInt(Y, yInterpolator.getInterpolation(ratio)) + Math.max(0, (editHeightDelta - editCompensation)));
            }

            invalidate();
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
//                Log.v("GUB", "onAnimationStart: text=" + getMessageObject().messageText);
                isAnimationStarted = true;
                AndroidUtilities.runOnUIThread(() -> delegate.onAnimationStart(AnimatedChatMessageCell.this));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimationFinished = true;
                checkAnimationFinish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // stub
            }
        });
        animator.start();
    }

    private void cancelAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    private void startCorrectionAnimation() {
        isCorrectionAnimationStarted = true;
//        Log.v("GUB", "startCorrectionAnimation: " + currentMessageObject.messageText + " / " + getY() + " / " + getEndY());
        parameters.put(CORRECTED_Y, new Integer[] { (int) getY(), getEndY() });

        correctionAnimator = ValueAnimator.ofFloat(0, 1);
        correctionAnimator.setDuration(delegate.getGlobalRemainingDuration());
        correctionAnimator.addUpdateListener(animation -> {
            float ratio = (float) animation.getAnimatedValue();
            setY(lerpInt(CORRECTED_Y, ratio));
            invalidate();
        });
        correctionAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isCorrectionAnimationFinished = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isCorrectionAnimationFinished = true;
                checkAnimationFinish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // stub
            }
        });
        correctionAnimator.start();
    }

    private void cancelCorrectionAnimation() {
        if (correctionAnimator != null) {
            correctionAnimator.cancel();
            correctionAnimator = null;
        }
    }

    private float lerpInt(int id, float ratio) {
        Integer[] ints = (Integer[]) parameters.get(id);
        return AndroidUtilities.lerp(ints[0], ints[1], ratio);
    }

    private float lerpFloat(int id, float ratio) {
        Float[] floats = (Float[]) parameters.get(id);
        return AndroidUtilities.lerp(floats[0], floats[1], ratio);
    }

    private void lerpInts(int id, float ratio, float[] result) {
        Integer[][] ints = (Integer[][]) parameters.get(id);
        for (int i = 0; i < result.length; ++i) {
            result[i] = AndroidUtilities.lerp(ints[0][i], ints[1][i], ratio);
        }
    }

    private void lerpFloats(int id, float ratio, float[] result) {
        Float[][] ints = (Float[][]) parameters.get(id);
        for (int i = 0; i < result.length; ++i) {
            result[i] = AndroidUtilities.lerp(ints[0][i], ints[1][i], ratio);
        }
    }

    @ColorInt
    private int blendColor(int id, float ratio) {
        Integer[] colors = (Integer[]) parameters.get(id);
        return ColorUtils.blendARGB(colors[0], colors[1], ratio);
    }

    private int getEndY() {
        return realCell.getTop() - AndroidUtilities.dp(1);
    }

    public interface Delegate {
        void onAnimationStart(AnimatedChatMessageCell cell);
        void onAnimationEnd(AnimatedChatMessageCell cell);

        TextSelectionHelper.ChatListTextSelectionHelper getTextSelectionHelper();
        ChatActivityEnterView getChatActivityEnterView();
        FrameLayout getAnimatedMessagesOverlay();
        RecyclerListView getChatListView();

        int getGlobalRemainingDuration();
    }
}
