package ru.touchin.roboswag.core.observables.collections.loadable;

import android.support.annotation.Nullable;

public class LoadingFromRequest<TItemId> {

    @Nullable
    private final TItemId itemId;
    private final int position;

    public LoadingFromRequest(@Nullable final TItemId itemId, final int position) {
        this.itemId = itemId;
        this.position = position;
    }

    @Nullable
    public TItemId getFromItemId() {
        return itemId;
    }

    public int getFromItemPosition() {
        return position;
    }

}