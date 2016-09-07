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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.touchin.roboswag.core.log.Lc;
import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * {@link ObservableCollection} that is based on list.
 * So it is providing similar List's methods like adding/removing/clearing etc.
 * But! You can observe it's changes.
 *
 * @param <TItem> Type of collection's items.
 */
public class ObservableList<TItem> extends ObservableCollection<TItem> implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private List<TItem> items;

    public ObservableList() {
        super();
        items = new ArrayList<>();
    }

    public ObservableList(@NonNull final Collection<TItem> initialItems) {
        super();
        items = new ArrayList<>(initialItems);
    }

    /**
     * Adding item at the end of list.
     *
     * @param item Item to add.
     */
    public void add(@NonNull final TItem item) {
        add(items.size(), item);
    }

    /**
     * Adding item at specific list position.
     *
     * @param position Position to add item to;
     * @param item     Item to add.
     */
    public void add(final int position, @NonNull final TItem item) {
        synchronized (this) {
            items.add(position, item);
            notifyAboutChange(new Change<>(Change.Type.INSERTED, Collections.singleton(item), position));
        }
    }

    /**
     * Adding items at the end of list.
     *
     * @param itemsToAdd Items to add.
     */
    public void addAll(@NonNull final Collection<TItem> itemsToAdd) {
        addAll(items.size(), itemsToAdd);
    }

    /**
     * Adding items at specific list position.
     *
     * @param position   Position to add items to;
     * @param itemsToAdd Items to add.
     */
    public void addAll(final int position, @NonNull final Collection<TItem> itemsToAdd) {
        synchronized (this) {
            if (!itemsToAdd.isEmpty()) {
                items.addAll(position, itemsToAdd);
                notifyAboutChange(new Change<>(Change.Type.INSERTED, itemsToAdd, position));
            }
        }
    }

    /**
     * Removing item.
     *
     * @param item Item to remove.
     */
    public void remove(@NonNull final TItem item) {
        synchronized (this) {
            final int position = indexOf(item);
            if (position < 0) {
                Lc.assertion("Illegal removing of item " + item);
                return;
            }
            remove(position);
        }
    }

    /**
     * Removing item by position.
     *
     * @param position Position to remove item from.
     */
    public void remove(final int position) {
        remove(position, 1);
    }

    /**
     * Removing items by position.
     *
     * @param position Position to remove items from;
     * @param count    Count of items to remove.
     */
    public void remove(final int position, final int count) {
        if (count == 0) {
            return;
        }
        synchronized (this) {
            final List<TItem> changedItems = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                changedItems.add(items.get(position));
                items.remove(position);
            }
            notifyAboutChange(new Change<>(Change.Type.REMOVED, changedItems, position));
        }
    }

    /**
     * Removing all items from list.
     */
    public void clear() {
        synchronized (this) {
            final Change<TItem> change = new Change<>(Change.Type.REMOVED, items, 0);
            if (!change.getChangedItems().isEmpty()) {
                items.clear();
                notifyAboutChange(change);
            }
        }
    }

    @NonNull
    @Override
    public TItem get(final int position) {
        synchronized (this) {
            return items.get(position);
        }
    }

    @NonNull
    @Override
    public Collection<TItem> getItems() {
        synchronized (this) {
            return Collections.unmodifiableCollection(new ArrayList<>(items));
        }
    }

    /**
     * Replace item at specific position.
     *
     * @param position Position to replace item;
     * @param item     Item to place.
     */
    public void update(final int position, @NonNull final TItem item) {
        update(position, Collections.singleton(item));
    }

    /**
     * Replace items at specific position.
     *
     * @param position     Position to replace items;
     * @param updatedItems Items to place.
     */
    public void update(final int position, @NonNull final Collection<TItem> updatedItems) {
        if (updatedItems.isEmpty()) {
            return;
        }
        int index = position;
        synchronized (this) {
            for (final TItem item : updatedItems) {
                items.set(index, item);
                index++;
            }
            notifyAboutChange(new Change<>(Change.Type.CHANGED, updatedItems, position));
        }
    }

    /**
     * Resetting all items in list to new ones.
     *
     * @param newItems New items to set.
     */
    public void set(@NonNull final Collection<TItem> newItems) {
        synchronized (this) {
            final Collection<Change<TItem>> changes = Change.calculateCollectionChanges(items, newItems, false);
            items.clear();
            items.addAll(newItems);
            if (!changes.isEmpty()) {
                notifyAboutChanges(changes);
            }
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return items.size();
        }
    }

    /**
     * Returns position of item in list.
     *
     * @param item Item to find index of;
     * @return Position of item in list or -1 if item not found.
     */
    public int indexOf(@NonNull final TItem item) {
        synchronized (this) {
            return items.indexOf(item);
        }
    }

    @NonNull
    @Override
    public Observable<TItem> loadItem(final int position) {
        synchronized (this) {
            return position < items.size() ? Observable.just(items.get(position)) : Observable.just(null);
        }
    }

    private void writeObject(@NonNull final ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(items);
    }

    @SuppressWarnings("unchecked")
    private void readObject(@NonNull final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        items = (List<TItem>) inputStream.readObject();
    }

}
