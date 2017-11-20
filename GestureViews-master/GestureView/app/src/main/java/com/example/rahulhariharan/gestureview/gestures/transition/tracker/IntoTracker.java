package com.example.rahulhariharan.gestureview.gestures.transition.tracker;

public interface IntoTracker<ID> extends AbstractTracker<ID> {

    /**
     * @return Item's id at given position, or {@code null} if position is invalid.
     * Note, that only one id per position should be possible for "To" view.
     */
    ID getIdByPosition(int position);

}
