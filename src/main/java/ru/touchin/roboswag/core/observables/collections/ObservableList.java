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
        notifyAboutChange(new Change(Change.Type.INSERTED, position, 1));
    }

    public void addAll(@NonNull final Collection<TItem> itemsToAdd) {
        addAll(items.size(), itemsToAdd);
    }

    public void addAll(final int position, @NonNull final Collection<TItem> itemsToAdd) {
        items.addAll(position, itemsToAdd);
        notifyAboutChange(new Change(Change.Type.INSERTED, position, itemsToAdd.size()));
    }

    public void remove(final int position) {
        items.remove(position);
        notifyAboutChange(new Change(Change.Type.REMOVED, position, 1));
    }

    public void clear() {
        final int oldSize = items.size();
        items.clear();
        notifyAboutChange(new Change(Change.Type.REMOVED, 0, oldSize));
    }

    @NonNull
    @Override
    public TItem get(final int position) {
        return items.get(position);
    }

    public void set(final int position, @NonNull final TItem item) {
        items.set(position, item);
        notifyAboutChange(new Change(Change.Type.CHANGED, position, 1));
    }

    public void set(@NonNull final Collection<TItem> newItems) {
        int currentItemsStart = 0;
        for (final TItem item : newItems) {
            boolean found = false;
            for (int i = currentItemsStart; i < items.size(); i++) {
                if (item.equals(items.get(i))) {
                    found = true;
                    for (int j = currentItemsStart; j < i; j++) {
                        items.remove(currentItemsStart);
                    }
                    notifyAboutChange(new Change(Change.Type.REMOVED, currentItemsStart, i - currentItemsStart));
                    break;
                }
            }
            if (!found) {
                add(currentItemsStart, item);
            }
            currentItemsStart++;
        }

        if (currentItemsStart < items.size()) {
            final int itemsToRemove = items.size() - currentItemsStart;
            for (int i = 0; i < itemsToRemove; i++) {
                items.remove(currentItemsStart);
            }
            notifyAboutChange(new Change(Change.Type.REMOVED, currentItemsStart, itemsToRemove));
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
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
