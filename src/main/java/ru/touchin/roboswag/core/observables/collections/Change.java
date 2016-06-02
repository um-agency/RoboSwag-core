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

public class Change {

    @NonNull
    public static Collection<Change> calculateCollectionChanges(@NonNull final Collection initialCollection,
                                                                @NonNull final Collection modifiedCollection) {
        final Collection<Change> result = new ArrayList<>();

        int couldBeAdded = modifiedCollection.size() - initialCollection.size();
        int initialOffset = 0;
        int itemsToAdd = 0;
        int currentSize = 0;
        for (final Object modifiedObject : modifiedCollection) {
            boolean found = false;
            int initialPosition = 0;
            for (final Object initialObject : initialCollection) {
                if (initialPosition < initialOffset) {
                    initialPosition++;
                    continue;
                }
                if (modifiedObject.equals(initialObject)) {
                    if (itemsToAdd > 0) {
                        if (couldBeAdded < itemsToAdd) {
                            result.add(new Change(Type.CHANGED, currentSize, modifiedCollection.size() - currentSize));
                            final int overSize = initialCollection.size() - modifiedCollection.size();
                            if (overSize > 0) {
                                result.add(new Change(Type.REMOVED, modifiedCollection.size(), overSize));
                            }
                            return result;
                        }
                        result.add(new Change(Type.INSERTED, currentSize, itemsToAdd));
                        couldBeAdded -= itemsToAdd;
                        itemsToAdd = 0;
                    }
                    found = true;
                    final int itemsToRemove = initialPosition - initialOffset;
                    if (itemsToRemove > 0) {
                        if (couldBeAdded < -itemsToRemove) {
                            result.add(new Change(Type.CHANGED, currentSize, modifiedCollection.size() - currentSize));
                            final int overSize = initialCollection.size() - modifiedCollection.size();
                            if (overSize > 0) {
                                result.add(new Change(Type.REMOVED, modifiedCollection.size(), overSize));
                            }
                            return result;
                        }
                        result.add(new Change(Change.Type.REMOVED, currentSize, itemsToRemove));
                        couldBeAdded += itemsToRemove;
                    }
                    initialOffset = initialPosition + 1;
                    currentSize++;
                    break;
                }
                initialPosition++;
            }
            if (!found) {
                itemsToAdd++;
            }
        }

        if (itemsToAdd > 0) {
            if (couldBeAdded < itemsToAdd) {
                result.add(new Change(Type.CHANGED, currentSize, modifiedCollection.size() - currentSize));
                final int overSize = initialCollection.size() - modifiedCollection.size();
                if (overSize > 0) {
                    result.add(new Change(Type.REMOVED, modifiedCollection.size(), overSize));
                }
                return result;
            }
            result.add(new Change(Type.INSERTED, currentSize, itemsToAdd));
            currentSize += itemsToAdd;
            couldBeAdded -= itemsToAdd;
        }

        final int itemsToRemove = initialCollection.size() - currentSize;
        if (itemsToRemove > 0) {
            if (couldBeAdded < -itemsToRemove) {
                result.add(new Change(Type.CHANGED, currentSize, modifiedCollection.size() - currentSize));
                final int overSize = initialCollection.size() - modifiedCollection.size();
                if (overSize > 0) {
                    result.add(new Change(Type.REMOVED, modifiedCollection.size(), overSize));
                }
                return result;
            }
            result.add(new Change(Change.Type.REMOVED, currentSize, initialCollection.size() - currentSize));
        }

        return result;
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

}