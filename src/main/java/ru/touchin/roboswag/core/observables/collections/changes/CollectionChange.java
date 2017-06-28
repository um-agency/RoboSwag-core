package ru.touchin.roboswag.core.observables.collections.changes;

import android.support.annotation.NonNull;

import java.util.Collection;

/**
 * Class which is representing change of collection. There could be multiple changes applied to collection.
 */
public class CollectionChange<TItem> {

    private final int number;
    @NonNull
    private final Collection<Change> changes;

    public CollectionChange(final int number, @NonNull final Collection<Change> changes) {
        this.number = number;
        this.changes = changes;
    }

    /**
     * Returns number of change.
     *
     * @return Number of change.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns collection of changes.
     *
     * @return Collection of changes.
     */
    @NonNull
    public Collection<Change> getChanges() {
        return changes;
    }

}
