package ru.touchin.roboswag.core.observables.collections.changes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.touchin.roboswag.core.utils.diff.DiffUtil;
import ru.touchin.roboswag.core.utils.diff.ListUpdateCallback;

public class DiffCollectionsChangesCalculator<TItem> extends DiffUtil.Callback implements CollectionsChangesCalculator {

    @NonNull
    private final List<TItem> oldList;
    @NonNull
    private final List<TItem> newList;
    @NonNull
    private final SameItemsPredicate<TItem> sameItemsPredicate;

    public DiffCollectionsChangesCalculator(@NonNull final List<TItem> oldList,
                                            @NonNull final List<TItem> newList,
                                            @NonNull final SameItemsPredicate<TItem> sameItemsPredicate) {
        this.oldList = oldList;
        this.newList = newList;
        this.sameItemsPredicate = sameItemsPredicate;
    }

    @NonNull
    @Override
    public Collection<Change> calculateChanges() {
        final Collection<Change> changes = new ArrayList<>();
        DiffUtil.calculateDiff(this).dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(final int position, final int count) {
                changes.add(new Change.Inserted(position, count));
            }

            @Override
            public void onRemoved(final int position, final int count) {
                changes.add(new Change.Removed(position, count));
            }

            @Override
            public void onMoved(final int fromPosition, final int toPosition) {
                changes.add(new Change.Moved(fromPosition, toPosition));
            }

            @Override
            public void onChanged(final int position, final int count, @Nullable final Object payload) {
                changes.add(new Change.Changed(position, count, payload));
            }
        });
        return changes;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
        return sameItemsPredicate.test(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

}
