/*
 *  Copyright (c) 2017 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.core.observables.collections.changes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.touchin.roboswag.core.android.support.v7.util.DiffUtil;
import ru.touchin.roboswag.core.android.support.v7.util.ListUpdateCallback;

/**
 * Implementation of {@link CollectionsChangesCalculator} based on DiffUtils from support library.
 */
public class DiffCollectionsChangesCalculator<TItem> extends DiffUtil.Callback implements CollectionsChangesCalculator<TItem> {

    @NonNull
    private final List<TItem> oldList;
    @NonNull
    private final List<TItem> newList;
    private final boolean detectMoves;
    @NonNull
    private final SameItemsPredicate<TItem> sameItemsPredicate;
    @Nullable
    private final ChangePayloadProducer<TItem> changePayloadProducer;

    public DiffCollectionsChangesCalculator(@NonNull final List<TItem> oldList,
                                            @NonNull final List<TItem> newList,
                                            final boolean detectMoves,
                                            @NonNull final SameItemsPredicate<TItem> sameItemsPredicate,
                                            @Nullable final ChangePayloadProducer<TItem> changePayloadProducer) {
        super();
        this.oldList = oldList;
        this.newList = newList;
        this.detectMoves = detectMoves;
        this.sameItemsPredicate = sameItemsPredicate;
        this.changePayloadProducer = changePayloadProducer;
    }

    @NonNull
    @Override
    public List<Change> calculateChanges() {
        final List<Change> changes = new ArrayList<>();
        DiffUtil.calculateDiff(this, detectMoves).dispatchUpdatesTo(new ListUpdateCallback() {
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

    @NonNull
    @Override
    public List<TItem> calculateInsertedItems() {
        final List<TItem> insertedItems = new ArrayList<>();
        for (final TItem newItem : newList) {
            if (!containsByPredicate(newItem, oldList)) {
                insertedItems.add(newItem);
            }
        }
        return insertedItems;
    }

    @NonNull
    @Override
    public List<TItem> calculateRemovedItems() {
        final List<TItem> removedItems = new ArrayList<>();
        for (final TItem oldItem : oldList) {
            if (!containsByPredicate(oldItem, newList)) {
                removedItems.add(oldItem);
            }
        }
        return removedItems;
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
        return sameItemsPredicate.areSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(final int oldItemPosition, final int newItemPosition) {
        return changePayloadProducer != null
                ? changePayloadProducer.getChangePayload(oldList.get(oldItemPosition), newList.get(newItemPosition)) : null;
    }

    private boolean containsByPredicate(@NonNull final TItem searchedItem, @NonNull final List<TItem> items) {
        for (final TItem item : items) {
            if (sameItemsPredicate.areSame(item, searchedItem)) {
                return true;
            }
        }
        return false;
    }

}
