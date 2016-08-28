/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

package ru.touchin.roboswag.core.observables.collections;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * Class representing simple change of collection like insertion, remove or replacing/changing items.
 *
 * @param <TItem> Type of changing collection's items.
 */
public class Change<TItem> {

    /**
     * Method to calculate changes between two collections.
     *
     * @param initialCollection           Initial collection;
     * @param modifiedCollection          Changed collection;
     * @param shrinkChangesToModifiedSize Flag to make position of changed items be less then modified collection size.
     *                                    It is needed sometimes to not get exceptions like {@link ArrayIndexOutOfBoundsException}.
     * @param <TItem>                     Type of collections items.
     * @return Changes between collections.
     */
    @NonNull
    public static <TItem> Collection<Change<TItem>> calculateCollectionChanges(@NonNull final Collection<TItem> initialCollection,
                                                                               @NonNull final Collection<TItem> modifiedCollection,
                                                                               final boolean shrinkChangesToModifiedSize) {
        return new CollectionsChangesCalculator<>(initialCollection, modifiedCollection, shrinkChangesToModifiedSize).calculateChanges();
    }

    @NonNull
    private final Type type;
    @NonNull
    private final Collection<TItem> changedItems;
    private final int start;
    private final int count;

    public Change(@NonNull final Type type, @NonNull final Collection<TItem> changedItems, final int start) {
        this.type = type;
        this.changedItems = Collections.unmodifiableCollection(new ArrayList<>(changedItems));
        this.start = start;
        this.count = changedItems.size();
    }

    /**
     * Returns type of change.
     *
     * @return Type of change.
     */
    @NonNull
    public Type getType() {
        return type;
    }

    /**
     * Returns collection of items which this change applied to.
     *
     * @return Changed items.
     */
    @NonNull
    public Collection<TItem> getChangedItems() {
        return changedItems;
    }

    /**
     * Returns first index of changed item.
     *
     * @return Start of change.
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns count of changed items.
     *
     * @return Count of changed items.
     */
    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return type + " change of " + start + ":" + count;
    }

    /**
     * Type of change.
     */
    public enum Type {
        INSERTED,
        CHANGED,
        REMOVED
    }

    private static class CollectionsChangesCalculator<TItem> {

        @NonNull
        private final Collection<TItem> initialCollection;
        @NonNull
        private final Collection<TItem> modifiedCollection;
        private final boolean shrinkChangesToModifiedSize;
        private int initialOffset;
        @NonNull
        private final Collection<TItem> itemsToAdd = new ArrayList<>();
        private int currentSize;
        private int oldSize;
        private int newSize;
        private int couldBeAdded;

        public CollectionsChangesCalculator(@NonNull final Collection<TItem> initialCollection,
                                            @NonNull final Collection<TItem> modifiedCollection,
                                            final boolean shrinkChangesToModifiedSize) {
            this.initialCollection = initialCollection;
            this.modifiedCollection = modifiedCollection;
            this.shrinkChangesToModifiedSize = shrinkChangesToModifiedSize;
        }

        @NonNull
        public Collection<Change<TItem>> calculateChanges() {
            initialOffset = 0;
            itemsToAdd.clear();
            currentSize = 0;
            oldSize = initialCollection.size();
            newSize = modifiedCollection.size();
            couldBeAdded = modifiedCollection.size() - initialCollection.size();
            final List<Change<TItem>> result = new ArrayList<>();
            for (final TItem modifiedItem : modifiedCollection) {
                int foundPosition = 0;
                for (final Object initialObject : initialCollection) {
                    if (foundPosition >= initialOffset && modifiedItem.equals(initialObject)) {
                        if (tryAddSkipped(result) == MethodAction.RETURN
                                || tryRemoveRest(result, initialOffset, foundPosition - initialOffset) == MethodAction.RETURN) {
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
            tryRemoveRest(result, initialOffset, initialCollection.size() - initialOffset);
            return result;
        }

        @NonNull
        private MethodAction tryAddSkipped(@NonNull final Collection<Change<TItem>> changes) {
            if (!itemsToAdd.isEmpty()) {
                if (shrinkChangesToModifiedSize && couldBeAdded < itemsToAdd.size()) {
                    addSimpleDifferenceChanges(changes);
                    return MethodAction.RETURN;
                }
                changes.add(new Change<>(Type.INSERTED, itemsToAdd, currentSize));
                currentSize += itemsToAdd.size();
                couldBeAdded -= itemsToAdd.size();
                itemsToAdd.clear();
            }
            return MethodAction.CONTINUE;
        }

        @NonNull
        private MethodAction tryRemoveRest(@NonNull final Collection<Change<TItem>> changes,
                                           final int initialOffset,
                                           final int itemsToRemove) {
            if (itemsToRemove > 0) {
                if (shrinkChangesToModifiedSize && couldBeAdded < -itemsToRemove) {
                    addSimpleDifferenceChanges(changes);
                    return MethodAction.RETURN;
                }
                changes.add(new Change<>(Change.Type.REMOVED,
                        new ArrayList<>(initialCollection).subList(initialOffset, initialOffset + itemsToRemove),
                        currentSize));
            }
            return MethodAction.CONTINUE;
        }

        private void addSimpleDifferenceChanges(@NonNull final Collection<Change<TItem>> changes) {
            changes.add(new Change<>(Type.CHANGED, new ArrayList<>(modifiedCollection).subList(currentSize, newSize), currentSize));
            if (oldSize - newSize > 0) {
                changes.add(new Change<>(Type.REMOVED, new ArrayList<>(initialCollection).subList(newSize, oldSize), newSize));
            }
        }

        private enum MethodAction {
            RETURN,
            CONTINUE
        }

    }

}