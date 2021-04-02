package org.telegram.ui.Cells;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.EditTextCaption;
import org.telegram.ui.Components.RecyclerListView;

public class AnimatedChatMessageCell extends ChatMessageCell {

    private final static int X = 0;
    private final static int Y = 1;
    private final static int COLOR_BACKGROUND = 2;
    private final static int BUBBLE_BACKGROUND_WIDTH = 3;
    private final static int TEXT_SIZE = 4;

    private final Delegate delegate;

    private ValueAnimator animator;
    private ChatMessageCell realCell;

    private final int[] startOverlayLocation = new int[2];
    private final int[] chatLocation = new int[2];

    private SparseArray<Object[]> parameters = new SparseArray<>();

    private boolean isRealCellLayoutDone = false;
    private boolean isAnimationCorrected = true;
    private boolean isAnimationFinished = false;

    public AnimatedChatMessageCell(Context context, MessageObject obj, Delegate delegate) {
        super(context);
        isAnimated = true;

        setMessageObject(obj, getCurrentMessagesGroup(), isPinnedBottom(), isPinnedTop());
        this.delegate = delegate;
        setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
            @Override
            public TextSelectionHelper.ChatListTextSelectionHelper getTextSelectionHelper() {
                return delegate.getTextSelectionHelper();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        checkAnimationStart(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Theme.chat_msgTextPaint.setTextSize(AndroidUtilities.dp(SharedConfig.fontSize));
        super.onDraw(canvas);
        checkAnimationStart(false);
    }

    public void setRealCell(ChatMessageCell cell) {
        Log.v("GUB", "setRealCell: text=" + cell.getMessageObject().messageText + ", isDrawn=" + isDrawn);
        if (realCell != cell) {
            realCell = cell;
            realCell.setHiddenBecauseAnimated(true);
            realCell.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    isRealCellLayoutDone = true;
                    realCell.removeOnLayoutChangeListener(this);
                    checkAnimationStart(false);
                }
            });
        }
    }

    public void correctAnimation() {
        Log.v("GUB", "correctAnimation: text=" + getMessageObject().messageText + ", isAnimationCorrected=" + isAnimationCorrected);
        if (!isAnimationCorrected) {
            isAnimationCorrected = true;
            animator.cancel();
            animator = null;
            checkAnimationStart(true);
        }
    }

    public void endAnimation() {
        Log.v("GUB", "endAnimation: text=" + getMessageObject().messageText);
        if (animator == null) {
            return;
        }

        animator.cancel();
        AndroidUtilities.runOnUIThread(() -> {
            if (realCell != null) {
                realCell.setHiddenBecauseAnimated(false);
            }
            delegate.onAnimationEnd(AnimatedChatMessageCell.this);
        });
    }

    public void dropAnimationCorrected() {
        Log.v("GUB", "dropAnimationCorrected: text=" + getMessageObject().messageText);
        isAnimationCorrected = false;
    }

    private void checkAnimationStart(boolean fromCurrentY) {
        if (animator == null && isDrawn && isRealCellLayoutDone && realCell != null) {
            startAnimation(fromCurrentY);
        }
    }

    private void checkAnimationFinish() {
        Log.v("GUB", "checkAnimationFinish: isAnimationFinished=" + isAnimationFinished + ", getEndY()=" + getEndY() + ", getY()=" + getY());
        if (isAnimationFinished && getEndY() == getY()) {
            endAnimation();
        }
    }

    private void startAnimation(boolean fromCurrentY) {
        Log.v("GUB", "startAnimation: text=" + getMessageObject().messageText);
        // x


        // y
        EditTextCaption editText = delegate.getChatActivityEnterView().getMessageEditText();
        editText.getBaseline();
        int[] editLocation = new int[2];
        editText.getLocationOnScreen(editLocation);
        int[] realCellLocation = new int[2];
        realCell.getLocationOnScreen(realCellLocation);
        delegate.getAnimatedMessagesOverlay().getLocationOnScreen(startOverlayLocation);
        delegate.getChatListView().getLocationOnScreen(chatLocation);
        int startY = fromCurrentY ? (int) getY() : editLocation[1] + editText.getBaseline() - startOverlayLocation[1] - getBottomBaseline();
        parameters.put(Y, new Integer[] { startY, getEndY() });
        setY((Integer) parameters.get(Y)[0]);

        // bubble
        Log.v("GUB", "realCell.backgroundWidth=" + realCell.backgroundWidth + ", location[0]=" + editLocation[0] + ", ?=" + backgroundDrawableTop + " / " + getBackgroundDrawable().getBounds().top);
        parameters.put(BUBBLE_BACKGROUND_WIDTH, new Integer[] {
                realCell.backgroundWidth - editLocation[0] + backgroundDrawableLeft + AndroidUtilities.dp(11) + getExtraTextX(),
                realCell.backgroundWidth
        });

        // text
        parameters.put(TEXT_SIZE, new Float[] {
                editText.getTextSize(),
                (float) AndroidUtilities.dp(SharedConfig.fontSize)
        });

        // colors
        int backgroundColor = Theme.getColor(Theme.key_chat_outBubble);
        parameters.put(COLOR_BACKGROUND, new Integer[] { ColorUtils.setAlphaComponent(backgroundColor, 128), backgroundColor });

        // animation
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(3000);
        animator.addUpdateListener(animation -> {
            float ratio = (float) animation.getAnimatedValue();
            // y
            int[] overlayLocation = new int[2];
            delegate.getAnimatedMessagesOverlay().getLocationOnScreen(overlayLocation);
            setY(lerpInt(Y, ratio));

            // bubble
            backgroundWidth = (int) lerpInt(BUBBLE_BACKGROUND_WIDTH, ratio);

            // text
            for (int i = 0; i < currentMessageObject.textLayoutBlocks.size(); ++i) {
                MessageObject.TextLayoutBlock block = currentMessageObject.textLayoutBlocks.get(i);
                block.textYOffset = 0;
            }
            textSize = lerpFloat(TEXT_SIZE, ratio);

            // color
            backgroundPaint.setColor(blendColor(COLOR_BACKGROUND, ratio));

            invalidate();
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                //Log.v("GUB", "onAnimationStart: text=" + getMessageObject().messageText);
                isAnimationFinished = false;
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

    private float lerpInt(int id, float ratio) {
        Integer[] ints = (Integer[]) parameters.get(id);
        return AndroidUtilities.lerp(ints[0], ints[1], ratio);
    }

    private float lerpFloat(int id, float ratio) {
        Float[] floats = (Float[]) parameters.get(id);
        return AndroidUtilities.lerp(floats[0], floats[1], ratio);
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
    }
}
