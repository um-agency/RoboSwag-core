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

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public class ObservableList<TItem> extends ObservableCollection<TItem> {

    @NonNull
    private final List<TItem> items;

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
        items.add(position, item);
        notifyAboutChange(new Change<>(Change.Type.INSERTED, Collections.singleton(item), position));
    }

    public void addAll(@NonNull final Collection<TItem> itemsToAdd) {
        addAll(items.size(), itemsToAdd);
    }

    public void addAll(final int position, @NonNull final Collection<TItem> itemsToAdd) {
        items.addAll(position, itemsToAdd);
        if (!itemsToAdd.isEmpty()) {
            notifyAboutChange(new Change<>(Change.Type.INSERTED, itemsToAdd, position));
        }
    }

    public void remove(final int position) {
        remove(position, 1);
    }

    public void remove(final int position, final int count) {
        final List<TItem> changedItems = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            changedItems.add(items.get(position));
            items.remove(position);
        }
        notifyAboutChange(new Change<>(Change.Type.REMOVED, changedItems, position));
    }

    public void clear() {
        final Change<TItem> change = new Change<>(Change.Type.REMOVED, items, 0);
        items.clear();
        if (!change.getChangedItems().isEmpty()) {
            notifyAboutChange(change);
        }
    }

    @NonNull
    @Override
    public TItem get(final int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public Collection<TItem> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    public void update(final int position, @NonNull final TItem item) {
        update(position, Collections.singleton(item));
    }

    public void update(final int position, @NonNull final Collection<TItem> updatedItems) {
        int i = position;
        for (final TItem item : updatedItems) {
            items.set(i, item);
            i++;
        }
        notifyAboutChange(new Change<>(Change.Type.CHANGED, updatedItems, position));
    }

    public void set(@NonNull final Collection<TItem> newItems) {
        final Collection<Change<TItem>> changes = Change.calculateCollectionChanges(items, newItems, false);
        items.clear();
        items.addAll(newItems);
        if (changes.size() > 0) {
            notifyAboutChanges(changes);
        }
    }

    @Override
    public int size() {
        return items.size();
    }

    @NonNull
    @Override
    public Observable<TItem> loadItem(final int position) {
        return position < items.size() ? Observable.just(items.get(position)) : Observable.just(null);
    }

}
