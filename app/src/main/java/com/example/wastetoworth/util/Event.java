package com.example.wastetoworth.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
public class Event<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Returns the content and prevents its use again.
     */
    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public T peekContent() {
        return content;
    }

    /**
     * Returns whether the event has been handled.
     */
    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }

    @NonNull
    @Override
    public String toString() {
        return "Event{" +
                "content=" + content +
                ", hasBeenHandled=" + hasBeenHandled +
                '}';
    }
}
