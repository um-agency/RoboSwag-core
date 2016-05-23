package ru.touchin.roboswag.core.observables.collections;

import android.support.annotation.NonNull;

public class Change {

    @NonNull
    private final Type type;
    private final int start;
    private final int count;

    public Change(@NonNull final Type type, final int start, final int count) {
        this.type = type;
        this.start = start;
        this.count = count;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    public int getStart() {
        return start;
    }

    public int getCount() {
        return count;
    }

    public enum Type {
        INSERTED,
        CHANGED,
        REMOVED
    }

}