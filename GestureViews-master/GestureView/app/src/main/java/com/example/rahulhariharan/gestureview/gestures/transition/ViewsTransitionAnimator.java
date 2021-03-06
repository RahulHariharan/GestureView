package com.example.rahulhariharan.gestureview.gestures.transition;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import com.example.rahulhariharan.gestureview.gestures.animation.ViewPosition;
import com.example.rahulhariharan.gestureview.gestures.animation.ViewPositionAnimator;
import com.example.rahulhariharan.gestureview.gestures.internal.GestureDebug;
import com.example.rahulhariharan.gestureview.gestures.views.interfaces.AnimatorView;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of {@link ViewsCoordinator} that allows requesting {@link #enter(Object, boolean)} or
 * {@link #exit(boolean)} animations, keeps track of {@link PositionUpdateListener} listeners
 * and provides correct implementation of {@link #isLeaving()}.
 * <p/>
 * Usage of this class should be similar to {@link ViewPositionAnimator} class.
 */
public class ViewsTransitionAnimator<ID> extends ViewsCoordinator<ID> {

    private static final Object NONE = new Object();

    private static final String TAG = ViewsTransitionAnimator.class.getSimpleName();

    private final List<ViewPositionAnimator.PositionUpdateListener> listeners = new ArrayList<>();

    private boolean enterWithAnimation;
    private boolean isEntered;

    private boolean exitRequested;
    private boolean exitWithAnimation;

    /**
     * @deprecated Use {@link GestureTransitions} instead.
     */
    @SuppressWarnings("WeakerAccess") // Public API
    @Deprecated
    public ViewsTransitionAnimator() {
        addPositionUpdateListener(new ViewPositionAnimator.PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                if (position == 0f && isLeaving) {
                    cleanupRequest();
                }
            }
        });
    }

    /**
     * Requests 'from' and 'to' views for given ID and starts enter animation when views are ready.
     *
     * @see ViewsCoordinator
     */
    public void enter(@NonNull ID id, boolean withAnimation) {
        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Enter requested for " + id + ", with animation = " + withAnimation);
        }

        enterWithAnimation = withAnimation;
        request(id);
    }

    /**
     * Similar to {@link #enter(Object, boolean) enter(ID, boolean)} but starts entering from no
     * specific id.<br/>
     * <b>Do not use this method if you are actually going to use items ids in {@link FromTracker}
     * or {@link IntoTracker}.</b><p/>
     * Can be used if your have single 'from' item with no specific id, like:<br/>
     * {@code GestureTransitions.from(imageView).into(gestureImageView).enterSingle(true)}
     */
    @SuppressWarnings("unchecked")
    public void enterSingle(boolean withAnimation) {
        // Passing 'NONE' Object instead of ID. Will fail if ID will be actually used.
        enter((ID) NONE, withAnimation);
    }

    /**
     * Plays exit animation, should only be called after corresponding call to
     * {@link #enter(Object, boolean)}.
     *
     * @see #isLeaving()
     */
    public void exit(boolean withAnimation) {
        if (getRequestedId() == null) {
            throw new IllegalStateException("You should call enter(...) before calling exit(...)");
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Exit requested from " + getRequestedId()
                    + ", with animation = " + withAnimation);
        }

        exitRequested = true;
        exitWithAnimation = withAnimation;
        exitIfRequested();
    }

    private void exitIfRequested() {
        if (exitRequested && isReady()) {
            exitRequested = false;

            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Perform exit from " + getRequestedId());
            }

            getToView().getPositionAnimator().exit(exitWithAnimation);
        }
    }

    /**
     * @return Whether 'enter' was not requested recently or animator is in leaving state.
     * Means that animation direction is from final (to) position back to initial (from) position.
     */
    public boolean isLeaving() {
        return exitRequested || getRequestedId() == null
                || (isReady() && getToView().getPositionAnimator().isLeaving());
    }


    /**
     * Adds listener to the set of position updates listeners that will be notified during
     * any position changes.
     *
     * @see ViewPositionAnimator#addPositionUpdateListener(PositionUpdateListener)
     */
    public void addPositionUpdateListener(@NonNull ViewPositionAnimator.PositionUpdateListener listener) {
        listeners.add(listener);
        if (isReady()) {
            getToView().getPositionAnimator().addPositionUpdateListener(listener);
        }
    }

    /**
     * Removes listener added by {@link #addPositionUpdateListener(PositionUpdateListener)}.
     *
     * @see ViewPositionAnimator#removePositionUpdateListener(PositionUpdateListener)
     */
    @SuppressWarnings("unused") // Public API
    public void removePositionUpdateListener(@NonNull ViewPositionAnimator.PositionUpdateListener listener) {
        listeners.remove(listener);
        if (isReady()) {
            getToView().getPositionAnimator().removePositionUpdateListener(listener);
        }
    }


    @Override
    public void setFromListener(@NonNull OnRequestViewListener<ID> listener) {
        super.setFromListener(listener);
        if (listener instanceof RequestListener) {
            ((RequestListener<ID>) listener).initAnimator(this);
        }
    }

    @Override
    public void setToListener(@NonNull OnRequestViewListener<ID> listener) {
        super.setToListener(listener);
        if (listener instanceof RequestListener) {
            ((RequestListener<ID>) listener).initAnimator(this);
        }
    }

    @Override
    protected void onFromViewChanged(@Nullable View fromView, @Nullable ViewPosition fromPos) {
        super.onFromViewChanged(fromView, fromPos);

        if (isReady()) {
            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Updating 'from' view for " + getRequestedId());
            }

            if (fromView != null) {
                getToView().getPositionAnimator().update(fromView);
            } else if (fromPos != null) {
                getToView().getPositionAnimator().update(fromPos);
            } else {
                getToView().getPositionAnimator().updateToNone();
            }
        }
    }

    @Override
    protected void onToViewChanged(@Nullable AnimatorView old, @NonNull AnimatorView view) {
        super.onToViewChanged(old, view);

        if (isReady() && old != null) {
            // Animation is in place, we should carefully swap animators
            swapAnimator(old.getPositionAnimator(), view.getPositionAnimator());
        } else {
            if (old != null) {
                cleanupAnimator(old.getPositionAnimator());
            }
            initAnimator(view.getPositionAnimator());
        }
    }

    @Override
    protected void onViewsReady(@NonNull ID id) {
        if (!isEntered) {
            isEntered = true;

            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Ready to enter for " + getRequestedId());
            }

            if (getFromView() != null) {
                getToView().getPositionAnimator().enter(getFromView(), enterWithAnimation);
            } else if (getFromPos() != null) {
                getToView().getPositionAnimator().enter(getFromPos(), enterWithAnimation);
            } else {
                getToView().getPositionAnimator().enter(enterWithAnimation);
            }

            exitIfRequested();
        }

        if (getFromView() instanceof ImageView && getToView() instanceof ImageView) {
            // Pre-setting 'to' image with 'from' image to prevent flickering
            ImageView from = (ImageView) getFromView();
            ImageView to = (ImageView) getToView();
            if (to.getDrawable() == null) {
                to.setImageDrawable(from.getDrawable());
            }
        }

        super.onViewsReady(id);
    }

    @Override
    protected void cleanupRequest() {
        if (getToView() != null) {
            cleanupAnimator(getToView().getPositionAnimator());
        }

        isEntered = false;
        exitRequested = false;

        super.cleanupRequest();
    }


    private void initAnimator(ViewPositionAnimator animator) {
        for (ViewPositionAnimator.PositionUpdateListener listener : listeners) {
            animator.addPositionUpdateListener(listener);
        }
    }

    private void cleanupAnimator(ViewPositionAnimator animator) {
        for (ViewPositionAnimator.PositionUpdateListener listener : listeners) {
            animator.removePositionUpdateListener(listener);
        }

        if (!animator.isLeaving() || animator.getPosition() != 0f) {
            if (GestureDebug.isDebugAnimator()) {
                Log.d(TAG, "Exiting from cleaned animator for " + getRequestedId());
            }

            animator.exit(false);
        }
    }

    /**
     * Replaces old animator with new one preserving state.
     */
    private void swapAnimator(ViewPositionAnimator old, ViewPositionAnimator next) {
        final float position = old.getPosition();
        final boolean isLeaving = old.isLeaving();
        final boolean isAnimating = old.isAnimating();

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Swapping animator for " + getRequestedId());
        }

        cleanupAnimator(old);

        if (getFromView() != null) {
            next.enter(getFromView(), false);
        } else if (getFromPos() != null) {
            next.enter(getFromPos(), false);
        }

        initAnimator(next);

        next.setState(position, isLeaving, isAnimating);
    }


    public abstract static class RequestListener<ID> implements OnRequestViewListener<ID> {
        private ViewsTransitionAnimator<ID> animator;

        protected void initAnimator(ViewsTransitionAnimator<ID> animator) {
            this.animator = animator;
        }

        protected ViewsTransitionAnimator<ID> getAnimator() {
            return animator;
        }
    }

}
