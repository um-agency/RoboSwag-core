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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default calculator between two collections that use equals function.
 */
public class DefaultCollectionsChangesCalculator<TItem> implements CollectionsChangesCalculator<TItem> {

    @NonNull
    private final Collection<TItem> initialCollection;
    @NonNull
    private final Collection<TItem> modifiedCollection;
    private final boolean shrinkChangesToModifiedSize;
    @NonNull
    private final Collection<TItem> itemsToAdd = new ArrayList<>();
    private int currentSize;
    private int oldSize;
    private int newSize;
    private int couldBeAdded;

    /**
     * Default calculator of changes between two collections.
     *
     * @param initialCollection           Initial collection;
     * @param modifiedCollection          Changed collection;
     * @param shrinkChangesToModifiedSize Flag to make position of changed items be less then modified collection size.
     *                                    It is needed sometimes to not get exceptions like {@link ArrayIndexOutOfBoundsException}.
     */
    public DefaultCollectionsChangesCalculator(@NonNull final Collection<TItem> initialCollection,
                                               @NonNull final Collection<TItem> modifiedCollection,
                                               final boolean shrinkChangesToModifiedSize) {
        super();
        this.initialCollection = initialCollection;
        this.modifiedCollection = modifiedCollection;
        this.shrinkChangesToModifiedSize = shrinkChangesToModifiedSize;
    }

    @NonNull
    @Override
    public List<Change> calculateChanges() {
        int initialOffset = 0;
        itemsToAdd.clear();
        currentSize = 0;
        oldSize = initialCollection.size();
        newSize = modifiedCollection.size();
        couldBeAdded = modifiedCollection.size() - initialCollection.size();
        final List<Change> result = new ArrayList<>();
        for (final TItem modifiedItem : modifiedCollection) {
            int foundPosition = 0;
            for (final Object initialObject : initialCollection) {
                if (foundPosition >= initialOffset && modifiedItem.equals(initialObject)) {
                    if (tryAddSkipped(result) == MethodAction.RETURN
                            || tryRemoveRest(result, foundPosition - initialOffset) == MethodAction.RETURN) {
                        return result;
                    }
                    initialOffset = foundPosition + 1;
                    currentSize++;
                    break;
                }
                foundPosition++;
            }
            // if not found
            if (foundPosition >= initialCollection.size()) {
                itemsToAdd.add(modifiedItem);
            }
        }

        if (tryAddSkipped(result) == MethodAction.RETURN) {
            return result;
        }
        tryRemoveRest(result, initialCollection.size() - initialOffset);
        return result;
    }

    @NonNull
    @Override
    public List<TItem> calculateInsertedItems() {
        final List<TItem> insertedItems = new ArrayList<>();
        for (final TItem newItem : modifiedCollection) {
            if (!initialCollection.contains(newItem)) {
                insertedItems.add(newItem);
            }
        }
        return insertedItems;
    }

    @NonNull
    @Override
    public List<TItem> calculateRemovedItems() {
        final List<TItem> removedItems = new ArrayList<>();
        for (final TItem oldItem : initialCollection) {
            if (!modifiedCollection.contains(oldItem)) {
                removedItems.add(oldItem);
            }
        }
        return removedItems;
    }

    @NonNull
    private MethodAction tryAddSkipped(@NonNull final Collection<Change> changes) {
        if (!itemsToAdd.isEmpty()) {
            if (shrinkChangesToModifiedSize && couldBeAdded < itemsToAdd.size()) {
                addSimpleDifferenceChanges(changes);
                return MethodAction.RETURN;
            }
            changes.add(new Change.Inserted(currentSize, itemsToAdd.size()));
            currentSize += itemsToAdd.size();
            couldBeAdded -= itemsToAdd.size();
            itemsToAdd.clear();
        }
        return MethodAction.CONTINUE;
    }

    @NonNull
    private MethodAction tryRemoveRest(@NonNull final Collection<Change> changes, final int itemsToRemove) {
        if (itemsToRemove > 0) {
            if (shrinkChangesToModifiedSize && couldBeAdded < -itemsToRemove) {
                addSimpleDifferenceChanges(changes);
                return MethodAction.RETURN;
            }
            changes.add(new Change.Removed(currentSize, itemsToRemove));
        }
        return MethodAction.CONTINUE;
    }

    private void addSimpleDifferenceChanges(@NonNull final Collection<Change> changes) {
        changes.add(new Change.Changed(currentSize, newSize - currentSize, null));
        if (oldSize - newSize > 0) {
            changes.add(new Change.Removed(newSize, oldSize - newSize));
        }
    }

    private enum MethodAction {
        RETURN,
        CONTINUE
    }

}
