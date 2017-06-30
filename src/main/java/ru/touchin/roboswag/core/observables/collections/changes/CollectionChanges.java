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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Class which is representing change of collection. There could be multiple changes applied to collection.
 */
public class CollectionChanges<TItem> {

    private final int number;
    @NonNull
    private final List<TItem> insertedItems;
    @NonNull
    private final List<TItem> removedItems;
    @NonNull
    private final Collection<Change> changes;

    public CollectionChanges(final int number,
                             @NonNull final List<TItem> insertedItems,
                             @NonNull final List<TItem> removedItems,
                             @NonNull final Collection<Change> changes) {
        this.number = number;
        this.insertedItems = Collections.unmodifiableList(insertedItems);
        this.removedItems = Collections.unmodifiableList(removedItems);
        this.changes = Collections.unmodifiableCollection(changes);
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

    /**
     * Returns inserted items in change.
     *
     * @return Inserted items.
     */
    @NonNull
    public List<TItem> getInsertedItems() {
        return insertedItems;
    }

    /**
     * Returns removed items in change.
     *
     * @return Removed items.
     */
    @NonNull
    public List<TItem> getRemovedItems() {
        return removedItems;
    }

}
