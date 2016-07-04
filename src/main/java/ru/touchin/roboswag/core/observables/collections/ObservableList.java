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

    public void add(@NonNull final TItem item) {
        add(items.size(), item);
    }

    public void add(final int position, @NonNull final TItem item) {
        synchronized (this) {
            items.add(position, item);
            notifyAboutChange(new Change<>(Change.Type.INSERTED, Collections.singleton(item), position));
        }
    }

    public void addAll(@NonNull final Collection<TItem> itemsToAdd) {
        addAll(items.size(), itemsToAdd);
    }

    public void addAll(final int position, @NonNull final Collection<TItem> itemsToAdd) {
        synchronized (this) {
            if (!itemsToAdd.isEmpty()) {
                items.addAll(position, itemsToAdd);
                notifyAboutChange(new Change<>(Change.Type.INSERTED, itemsToAdd, position));
            }
        }
    }

    public void remove(final int position) {
        remove(position, 1);
    }

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
            return Collections.unmodifiableCollection(items);
        }
    }

    public void update(final int position, @NonNull final TItem item) {
        update(position, Collections.singleton(item));
    }

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
