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

package ru.touchin.roboswag.core.observables.collections.loadable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.RxAndroidUtils;
import ru.touchin.roboswag.core.observables.collections.Change;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.observables.collections.ObservableList;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public class LoadingMoreList<TItem, TMoreReference, TLoadedItems extends LoadedItems<TItem, TMoreReference>>
        extends ObservableCollection<TItem> {

    @NonNull
    private final Scheduler loaderScheduler = RxAndroidUtils.createLooperScheduler();
    @NonNull
    private Observable<TLoadedItems> loadingMoreObservable;
    @NonNull
    private final BehaviorSubject<Integer> moreItemsCount = BehaviorSubject.create(LoadedItems.UNKNOWN_ITEMS_COUNT);
    @NonNull
    private final ObservableList<TItem> innerList = new ObservableList<>();
    private boolean removeDuplicates;
    @Nullable
    private TMoreReference moreItemsReference;

    public LoadingMoreList(@NonNull final MoreItemsLoader<TItem, TMoreReference, TLoadedItems> moreMoreItemsLoader) {
        this(moreMoreItemsLoader, null);
    }

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    //ConstructorCallsOverridableMethod: actually it is calling in lambda callback
    public LoadingMoreList(@NonNull final MoreItemsLoader<TItem, TMoreReference, TLoadedItems> moreMoreItemsLoader,
                           @Nullable final TLoadedItems initialItems) {
        super();
        this.loadingMoreObservable = Observable
                .switchOnNext(Observable.<Observable<TLoadedItems>>create(subscriber -> {
                    subscriber.onNext(moreMoreItemsLoader.load(new MoreLoadRequest<>(moreItemsReference, Math.max(0, size())))
                            .subscribeOn(Schedulers.io()));
                    subscriber.onCompleted();
                }).subscribeOn(loaderScheduler))
                .single()
                .doOnError(throwable -> {
                    if ((throwable instanceof IllegalArgumentException)
                            || (throwable instanceof NoSuchElementException)) {
                        Lc.assertion(new ShouldNotHappenException("Updates during loading not supported."
                                + " MoreItemsLoader should emit only one result.", throwable));
                    }
                })
                .observeOn(loaderScheduler)
                .doOnNext(loadedItems -> onItemsLoaded(loadedItems, false))
                .replay(1)
                .refCount();

        if (initialItems != null) {
            onItemsLoaded(initialItems, false);
        }
    }

    @NonNull
    protected final Scheduler getLoaderScheduler() {
        return loaderScheduler;
    }

    @NonNull
    @Override
    public Observable<CollectionChange<TItem>> observeChanges() {
        return innerList.observeChanges();
    }

    @Override
    protected void notifyAboutChanges(@NonNull final Collection<Change<TItem>> changes) {
        Lc.assertion("Illegal operation. Modify getInnerList()");
    }

    @NonNull
    protected ObservableList<TItem> getInnerList() {
        return innerList;
    }

    public boolean hasMoreItems() {
        return moreItemsCount.getValue() != 0;
    }

    @NonNull
    public Observable<Boolean> observeHasMoreItems() {
        return moreItemsCount.map(count -> count != 0).distinctUntilChanged();
    }

    @NonNull
    public Observable<Integer> observeMoreItemsCount() {
        return moreItemsCount.distinctUntilChanged();
    }

    public boolean isRemoveDuplicates() {
        return removeDuplicates;
    }

    public void setRemoveDuplicates(final boolean removeDuplicates) {
        this.removeDuplicates = removeDuplicates;
    }

    protected void onItemsLoaded(@NonNull final TLoadedItems loadedItems, final boolean reset) {
        moreItemsReference = loadedItems.getReference();
        final List<TItem> items = new ArrayList<>(loadedItems.getItems());
        if (!reset) {
            if (removeDuplicates) {
                removeDuplicatesFromList(items);
            }
            innerList.addAll(items);
        } else {
            innerList.set(items);
        }
        moreItemsCount.onNext(loadedItems.getMoreItemsCount());
    }

    protected void removeDuplicatesFromList(@NonNull final List<TItem> items) {
        for (int i = items.size() - 1; i >= 0; i--) {
            for (int j = 0; j < innerList.size(); j++) {
                if (innerList.get(j).equals(items.get(i))) {
                    items.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public int size() {
        return innerList.size();
    }

    @NonNull
    @Override
    public TItem get(final int position) {
        return innerList.get(position);
    }

    @NonNull
    @Override
    public Collection<TItem> getItems() {
        return innerList.getItems();
    }

    @NonNull
    protected Observable<TLoadedItems> getLoadingMoreObservable() {
        return loadingMoreObservable;
    }

    @NonNull
    @Override
    public Observable<TItem> loadItem(final int position) {
        return Observable
                .switchOnNext(Observable
                        .<Observable<TItem>>create(subscriber -> {
                            if (position < size()) {
                                subscriber.onNext(Observable.just(get(position)));
                            } else if (moreItemsCount.getValue() == 0) {
                                subscriber.onNext(Observable.just((TItem) null));
                            } else {
                                subscriber.onNext(loadingMoreObservable
                                        .switchMap(ignored -> Observable.<TItem>error(new NotLoadedYetException())));
                            }
                            subscriber.onCompleted();
                        })
                        .subscribeOn(loaderScheduler))
                .retryWhen(attempts -> attempts
                        .switchMap(throwable -> throwable instanceof NotLoadedYetException ? Observable.just(null) : Observable.error(throwable)));
    }

    public void reset() {
        innerList.clear();
        moreItemsCount.onNext(LoadedItems.UNKNOWN_ITEMS_COUNT);
    }

    public void reset(@NonNull final TLoadedItems initialItems) {
        onItemsLoaded(initialItems, true);
    }

    protected static class NotLoadedYetException extends Exception {
    }

}
