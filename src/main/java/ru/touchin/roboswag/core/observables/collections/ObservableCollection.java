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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
 * Class to represent collection which is providing it's inner changes in Rx observable way.
 * Use {@link #observeChanges()} and {@link #observeItems()} to observe collection changes.
 * Use {@link #loadItem(int)} to load item asynchronously.
 * Methods {@link #size()} and {@link #get(int)} will return only already loaded items info.
 *
 * @param <TItem> Type of collection's items.
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
    private Observable<CollectionChange<TItem>> createChangesObservable() {
        return Observable
                .<CollectionChange<TItem>>create(subscriber -> this.changesSubscriber = subscriber)
                .doOnUnsubscribe(() -> this.changesSubscriber = null)
                .replay(0)
                .refCount();
    }

    @NonNull
    private Observable<Collection<TItem>> createItemsObservable() {
        return Observable
                .<Collection<TItem>>switchOnNext(Observable.create(subscriber -> {
                    subscriber.onNext(observeChanges().map(changes -> getItems()).startWith(getItems()));
                    subscriber.onCompleted();
                }))
                .replay(1)
                .refCount();
    }

    /**
     * Return changes count number since collection creation.
     *
     * @return Changes count.
     */
    public int getChangesCount() {
        return changesCount;
    }

    /**
     * Method to notify that collection have changed.
     *
     * @param change Change of collection.
     */
    protected void notifyAboutChange(@NonNull final Change<TItem> change) {
        notifyAboutChanges(Collections.singleton(change));
    }

    /**
     * Method to notify that collection have changed.
     *
     * @param changes Changes of collection.
     */
    protected void notifyAboutChanges(@NonNull final Collection<Change<TItem>> changes) {
        changesCount++;
        if (changesSubscriber != null) {
            changesSubscriber.onNext(new CollectionChange<>(changesCount, Collections.unmodifiableCollection(changes)));
        }
    }

    /**
     * Observes changes so it can be used to update UI based on changes etc.
     *
     * @return List of changes applied to collection.
     */
    @NonNull
    public Observable<CollectionChange<TItem>> observeChanges() {
        return changesObservable;
    }

    /**
     * Returns already loaded item by position.
     * Use it carefully for collections which are loading asynchronously.
     *
     * @param position Position of item to get;
     * @return Item in collection by position.
     */
    @NonNull
    public abstract TItem get(int position);

    /**
     * Returns already loaded items.
     * Use it carefully for collections which are loading asynchronously.
     *
     * @return Collection of items.
     */
    @NonNull
    public abstract Collection<TItem> getItems();

    /**
     * Returns {@link Observable} to observe items collection.
     * Collection returned in onNext is not inner collection but it's copy, actually so you can't modify it.
     *
     * @return Collection's {@link Observable}.
     */
    @NonNull
    public Observable<Collection<TItem>> observeItems() {
        return itemsObservable;
    }

    /**
     * Returns size of already loaded items.
     *
     * @return Size.
     */
    public abstract int size();

    /**
     * Returns if already loaded items are empty or not.
     *
     * @return True if items are empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns {@link Observable} which is loading item by position.
     * It could return null in onNext callback if there is no item to load for such position.
     *
     * @param position Position to load item;
     * @return {@link Observable} to load item.
     */
    @NonNull
    public abstract Observable<TItem> loadItem(int position);

    /**
     * Returns {@link Observable} which is loading item by range.
     * It will return collection of loaded items in onNext callback.
     *
     * @param first First position of item to load;
     * @param last  Last position of item to load;
     * @return {@link Observable} to load items.
     */
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

    /**
     * Class which is representing change of collection. There could be multiple changes applied to collection.
     *
     * @param <TItem> Type of collection's items.
     */
    public static class CollectionChange<TItem> {

        private final int number;
        @NonNull
        private final Collection<Change<TItem>> changes;

        protected CollectionChange(final int number, @NonNull final Collection<Change<TItem>> changes) {
            this.number = number;
            this.changes = changes;
        }

        /**
         * Returns number of change.
         *
         * @return Number of change.
         */
        public int getNumber() {
            return number;
        }

        /**
         * Returns collection of changes.
         *
         * @return Collection of changes.
         */
        @NonNull
        public Collection<Change<TItem>> getChanges() {
            return changes;
        }

    }

}
