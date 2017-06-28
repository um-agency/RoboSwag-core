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
import java.util.Collection;
import java.util.Collections;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import ru.touchin.roboswag.core.observables.collections.changes.Change;
import ru.touchin.roboswag.core.observables.collections.changes.CollectionChange;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * Class to represent collection which is providing it's inner changes in Rx observable way.
 * Use {@link #observeChanges()} and {@link #observeItems()} to observe collection changes.
 * Methods {@link #size()} and {@link #get(int)} will return only already loaded items info.
 *
 * @param <TItem> Type of collection's items.
 */
public abstract class ObservableCollection<TItem> {

    private int changesCount;
    @NonNull
    private transient Observable<CollectionChange<TItem>> changesObservable;
    @NonNull
    private transient Observable<Collection<TItem>> itemsObservable;
    @Nullable
    private transient Emitter<? super CollectionChange<TItem>> changesEmitter;

    public ObservableCollection() {
        this.changesObservable = createChangesObservable();
        this.itemsObservable = createItemsObservable();
    }

    @NonNull
    private Observable<CollectionChange<TItem>> createChangesObservable() {
        return Observable
                .<CollectionChange<TItem>>create(emitter -> this.changesEmitter = emitter)
                .doOnDispose(() -> this.changesEmitter = null)
                .share();
    }

    @NonNull
    private Observable<Collection<TItem>> createItemsObservable() {
        return Observable
                //switchOnNext to calculate getItems() on subscription but not on that method calling moment
                .switchOnNext(Observable.fromCallable(() -> observeChanges().map(changes -> getItems()).startWith(getItems())))
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
    protected void notifyAboutChange(@NonNull final Change change) {
        notifyAboutChanges(Collections.singleton(change));
    }

    /**
     * Method to notify that collection have changed.
     *
     * @param changes Changes of collection.
     */
    protected void notifyAboutChanges(@NonNull final Collection<Change> changes) {
        if (changes.isEmpty()) {
            return;
        }
        changesCount++;
        if (changesEmitter != null) {
            changesEmitter.onNext(new CollectionChange<>(changesCount, Collections.unmodifiableCollection(changes)));
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

    private void writeObject(@NonNull final ObjectOutputStream outputStream) throws IOException {
        outputStream.writeInt(changesCount);
    }

    private void readObject(@NonNull final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        changesCount = inputStream.readInt();
        this.changesObservable = createChangesObservable();
        this.itemsObservable = createItemsObservable();
    }

}
