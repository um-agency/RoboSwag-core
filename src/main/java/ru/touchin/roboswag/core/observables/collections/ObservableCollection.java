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
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public abstract class ObservableCollection<TItem> implements Serializable {

    private int changesCount;
    @NonNull
    private transient Observable<CollectionChange<TItem>> changesObservable;
    @NonNull
    private transient Observable<Collection<TItem>> itemsObservable;
    @Nullable
    private transient Subscriber<? super CollectionChange<TItem>> changesSubscriber;

    public ObservableCollection() {
        this.changesObservable = createChangesObservable();
        this.itemsObservable = createItemsObservable();
    }

    @NonNull
    protected Observable<Collection<TItem>> createItemsObservable() {
        return Observable
                .<Collection<TItem>>create(subscriber -> {
                    subscriber.onNext(getItems());
                    subscriber.onCompleted();
                })
                .concatWith(changesObservable.map(changes -> getItems()))
                .replay(1)
                .refCount();
    }

    @NonNull
    private Observable<CollectionChange<TItem>> createChangesObservable() {
        return Observable
                .<CollectionChange<TItem>>create(subscriber -> this.changesSubscriber = subscriber)
                .doOnUnsubscribe(() -> this.changesSubscriber = null)
                .replay(0)
                .refCount();
    }

    public int getChangesCount() {
        return changesCount;
    }

    protected void notifyAboutChange(@NonNull final Change<TItem> change) {
        notifyAboutChanges(Collections.singleton(change));
    }

    protected void notifyAboutChanges(@NonNull final Collection<Change<TItem>> changes) {
        changesCount++;
        if (changesSubscriber != null) {
            changesSubscriber.onNext(new CollectionChange<>(changesCount, Collections.unmodifiableCollection(changes)));
        }
    }

    @NonNull
    public Observable<CollectionChange<TItem>> observeChanges() {
        return changesObservable;
    }

    @NonNull
    public abstract TItem get(int position);

    @NonNull
    public abstract Collection<TItem> getItems();

    @NonNull
    public Observable<Collection<TItem>> observeItems() {
        return itemsObservable;
    }

    public abstract int size();

    public boolean isEmpty() {
        return size() == 0;
    }

    @NonNull
    public abstract Observable<TItem> loadItem(int position);

    @NonNull
    public Observable<Collection<TItem>> loadRange(final int first, final int last) {
        final List<Observable<TItem>> itemsRequests = new ArrayList<>();
        for (int i = first; i <= last; i++) {
            itemsRequests.add(loadItem(i));
        }
        return Observable.concatEager(itemsRequests)
                .filter(loadedItem -> loadedItem != null)
                .toList()
                .map(Collections::unmodifiableCollection);
    }

    private void writeObject(@NonNull final ObjectOutputStream outputStream) throws IOException {
        outputStream.writeInt(changesCount);
    }

    private void readObject(@NonNull final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        changesCount = inputStream.readInt();
        this.changesObservable = createChangesObservable();
        this.itemsObservable = createItemsObservable();
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
