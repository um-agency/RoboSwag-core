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
import java.util.List;

import ru.touchin.roboswag.core.utils.ThreadLocalValue;

public class Change {

    private static final ThreadLocalValue<CollectionsChangesCalculator> COLLECTION_CHANGES_CALCULATOR
            = new ThreadLocalValue<>(CollectionsChangesCalculator::new);

    @NonNull
    public static Collection<Change> calculateCollectionChanges(@NonNull final Collection initialCollection,
                                                                @NonNull final Collection modifiedCollection) {
        return COLLECTION_CHANGES_CALCULATOR.get().calculateChanges(initialCollection, modifiedCollection);
    }

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

    @Override
    public String toString() {
        return type + " change of " + start + ":" + count;
    }

    public enum Type {
        INSERTED,
        CHANGED,
        REMOVED
    }

    private static class CollectionsChangesCalculator {

        private int initialOffset;
        private int itemsToAdd;
        private int currentSize;
        private int oldSize;
        private int newSize;
        private int couldBeAdded;

        @NonNull
        public Collection<Change> calculateChanges(@NonNull final Collection initialCollection,
                                                   @NonNull final Collection modifiedCollection) {
            initialOffset = 0;
            itemsToAdd = 0;
            currentSize = 0;
            oldSize = initialCollection.size();
            newSize = modifiedCollection.size();
            couldBeAdded = modifiedCollection.size() - initialCollection.size();
            final List<Change> result = new ArrayList<>();
            for (final Object modifiedObject : modifiedCollection) {
                int foundPosition = 0;
                for (final Object initialObject : initialCollection) {
                    if (foundPosition >= initialOffset && modifiedObject.equals(initialObject)) {
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
                if (foundPosition != initialOffset - 1) {
                    itemsToAdd++;
                }
            }

            if (tryAddSkipped(result) == MethodAction.RETURN) {
                return result;
            }
            tryRemoveRest(result, initialCollection.size() - currentSize);
            return result;
        }

        @NonNull
        private MethodAction tryAddSkipped(@NonNull final Collection<Change> changes) {
            if (itemsToAdd > 0) {
                if (couldBeAdded < itemsToAdd) {
                    addSimpleDifferenceChanges(changes);
                    return MethodAction.RETURN;
                }
                changes.add(new Change(Type.INSERTED, currentSize, itemsToAdd));
                currentSize += itemsToAdd;
                couldBeAdded -= itemsToAdd;
                itemsToAdd = 0;
            }
            return MethodAction.CONTINUE;
        }

        @NonNull
        private MethodAction tryRemoveRest(@NonNull final Collection<Change> changes, final int itemsToRemove) {
            if (itemsToRemove > 0) {
                if (couldBeAdded < -itemsToRemove) {
                    addSimpleDifferenceChanges(changes);
                    return MethodAction.RETURN;
                }
                changes.add(new Change(Change.Type.REMOVED, currentSize, itemsToRemove));
            }
            return MethodAction.CONTINUE;
        }

        private void addSimpleDifferenceChanges(@NonNull final Collection<Change> changes) {
            changes.add(new Change(Type.CHANGED, currentSize, newSize - currentSize));
            final int overSize = oldSize - newSize;
            if (overSize > 0) {
                changes.add(new Change(Type.REMOVED, newSize, overSize));
            }
        }

        private enum MethodAction {
            RETURN,
            CONTINUE
        }

    }

}