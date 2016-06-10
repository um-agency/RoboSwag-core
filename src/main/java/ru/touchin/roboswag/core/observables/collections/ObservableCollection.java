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
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public abstract class ObservableCollection<TItem> {

    private int changesCount;
    @NonNull
    private final PublishSubject<CollectionChange<TItem>> changesSubject = PublishSubject.create();

    public int getChangesCount() {
        return changesCount;
    }

    protected void notifyAboutChange(@NonNull final Change<TItem> change) {
        notifyAboutChanges(Collections.singleton(change));
    }

    protected void notifyAboutChanges(@NonNull final Collection<Change<TItem>> changes) {
        changesCount++;
        changesSubject.onNext(new CollectionChange<>(changesCount, changes));
    }

    @NonNull
    public Observable<CollectionChange<TItem>> observeChanges() {
        return changesSubject;
    }

    public abstract int size();

    @NonNull
    public abstract TItem get(int position);

    @NonNull
    public abstract Collection<TItem> getItems();

    @NonNull
    public abstract Observable<TItem> loadItem(int position);

    public boolean isEmpty() {
        return size() == 0;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public Observable<List<TItem>> loadRange(final int first, final int last) {
        final List<Observable<TItem>> itemsRequests = new ArrayList<>();
        for (int i = first; i <= last; i++) {
            itemsRequests.add(loadItem(i));
        }
        return Observable.concatEager(itemsRequests).toList().doOnNext(list -> {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i) == null) {
                    list.remove(i);
                }
            }
        });
    }

    public static class CollectionChange<TItem> {

        private final int number;
        @NonNull
        private final Collection<Change<TItem>> changes;

        protected CollectionChange(final int number, @NonNull final Collection<Change<TItem>> changes) {
            this.number = number;
            this.changes = changes;
        }

        public int getNumber() {
            return number;
        }

        @NonNull
        public Collection<Change<TItem>> getChanges() {
            return changes;
        }

    }

}
