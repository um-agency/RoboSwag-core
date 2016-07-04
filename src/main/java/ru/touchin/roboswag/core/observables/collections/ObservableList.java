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

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public class ObservableList<TItem> extends ObservableCollection<TItem> implements Serializable {

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

    public void add(@NonNull final TItem item) {
        add(items.size(), item);
    }

    public synchronized void add(final int position, @NonNull final TItem item) {
        items.add(position, item);
        notifyAboutChange(new Change<>(Change.Type.INSERTED, Collections.singleton(item), position));
    }

    public void addAll(@NonNull final Collection<TItem> itemsToAdd) {
        addAll(items.size(), itemsToAdd);
    }

    public synchronized void addAll(final int position, @NonNull final Collection<TItem> itemsToAdd) {
        if (!itemsToAdd.isEmpty()) {
            items.addAll(position, itemsToAdd);
            notifyAboutChange(new Change<>(Change.Type.INSERTED, itemsToAdd, position));
        }
    }

    public void remove(final int position) {
        remove(position, 1);
    }

    public synchronized void remove(final int position, final int count) {
        if (count == 0) {
            return;
        }
        final List<TItem> changedItems = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            changedItems.add(items.get(position));
            items.remove(position);
        }
        notifyAboutChange(new Change<>(Change.Type.REMOVED, changedItems, position));
    }

    public synchronized void clear() {
        final Change<TItem> change = new Change<>(Change.Type.REMOVED, items, 0);
        if (!change.getChangedItems().isEmpty()) {
            items.clear();
            notifyAboutChange(change);
        }
    }

    @NonNull
    @Override
    public synchronized TItem get(final int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public synchronized Collection<TItem> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    public void update(final int position, @NonNull final TItem item) {
        update(position, Collections.singleton(item));
    }

    public synchronized void update(final int position, @NonNull final Collection<TItem> updatedItems) {
        if (updatedItems.isEmpty()) {
            return;
        }
        int index = position;
        for (final TItem item : updatedItems) {
            items.set(index, item);
            index++;
        }
        notifyAboutChange(new Change<>(Change.Type.CHANGED, updatedItems, position));
    }

    public synchronized void set(@NonNull final Collection<TItem> newItems) {
        final Collection<Change<TItem>> changes = Change.calculateCollectionChanges(items, newItems, false);
        items.clear();
        items.addAll(newItems);
        if (!changes.isEmpty()) {
            notifyAboutChanges(changes);
        }
    }

    @Override
    public synchronized int size() {
        return items.size();
    }

    public synchronized int indexOf(@NonNull final TItem item) {
        return items.indexOf(item);
    }

    @NonNull
    @Override
    public synchronized Observable<TItem> loadItem(final int position) {
        return position < items.size() ? Observable.just(items.get(position)) : Observable.just(null);
    }

    private void writeObject(@NonNull final ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(items);
    }

    @SuppressWarnings("unchecked")
    private void readObject(@NonNull final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        items = (List<TItem>) inputStream.readObject();
    }

}
