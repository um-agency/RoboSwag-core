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

import java.util.NoSuchElementException;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * {@link ObservableCollection} which is loading items more and more by paging/limit-offset/reference-based mechanisms but also it is providing
 * interface to load newer items and info about it's loading availability.
 * To use this collection {@link MoreItemsLoader} and {@link NewerItemsLoader} should be created.
 *
 * @param <TItem>           Type of collection's items;
 * @param <TReference>      Type of reference object to help rightly loading next block of items;
 * @param <TNewerReference> Type of reference object to help rightly loading block of newer items;
 * @param <TLoadedItems>    Type of loading block of items.
 */
public class LoadingRenewableList<TItem, TReference, TNewerReference,
        TLoadedItems extends LoadedRenewableItems<TItem, TReference, TNewerReference>>
        extends LoadingMoreList<TItem, TReference, TLoadedItems> {

    @Nullable
    private TNewerReference newerReference;
    @NonNull
    private final BehaviorSubject<Integer> newerItemsCount = BehaviorSubject.create(LoadedItems.UNKNOWN_ITEMS_COUNT);

    @NonNull
    private final Observable<TLoadedItems> loadingNewerObservable;
    @NonNull
    private final Observable<TLoadedItems> loadingNewestObservable;

    public LoadingRenewableList(@NonNull final MoreItemsLoader<TItem, TReference, TLoadedItems> moreMoreItemsLoader,
                                @NonNull final NewerItemsLoader<TItem, TReference, TNewerReference, TLoadedItems> newerItemsLoader) {
        super(moreMoreItemsLoader);
        this.loadingNewerObservable = createLoadingNewerObservable(newerItemsLoader, false);
        this.loadingNewestObservable = createLoadingNewerObservable(newerItemsLoader, true);
    }

    public LoadingRenewableList(@NonNull final MoreItemsLoader<TItem, TReference, TLoadedItems> moreMoreItemsLoader,
                                @NonNull final NewerItemsLoader<TItem, TReference, TNewerReference, TLoadedItems> newerItemsLoader,
                                @Nullable final TLoadedItems initialItems) {
        super(moreMoreItemsLoader, initialItems);
        this.loadingNewerObservable = createLoadingNewerObservable(newerItemsLoader, false);
        this.loadingNewestObservable = createLoadingNewerObservable(newerItemsLoader, true);
        if (initialItems != null) {
            updateNewerReference(initialItems);
        }
    }

    @NonNull
    private Observable<TLoadedItems> waitForInitialLoading(@NonNull final Observable<TLoadedItems> observable) {
        return getLoadingMoreObservable().ignoreElements().concatWith(observable);
    }

    @NonNull
    private NewerLoadRequest<TNewerReference> createActualRequest() {
        return new NewerLoadRequest<>(newerReference, newerItemsCount.getValue());
    }

    @NonNull
    private Observable<TLoadedItems> createLoadingNewerObservable(
            @NonNull final NewerItemsLoader<TItem, TReference, TNewerReference, TLoadedItems> newerItemsLoader,
            final boolean renew) {
        return Observable
                .switchOnNext(Observable.<Observable<TLoadedItems>>create(subscriber -> {
                    if (!renew) {
                        subscriber.onNext(Observable.concat(
                                //we need non-empty list to star loading newer items or we need to wait any change (should be insertion)
                                isEmpty() ? observeChanges().first().switchMap(ignored -> Observable.empty()) : Observable.empty(),
                                createLoadRequestBasedObservable(this::createActualRequest,
                                        loadRequest -> loadRequest.getNewerReference() == null && isEmpty()
                                                ? waitForInitialLoading(newerItemsLoader.load(loadRequest))
                                                : newerItemsLoader.load(loadRequest))));
                    } else {
                        subscriber.onNext(newerItemsLoader.load(new NewerLoadRequest<>(null, LoadedItems.UNKNOWN_ITEMS_COUNT))
                                .subscribeOn(Schedulers.io())
                                .observeOn(getLoaderScheduler()));
                    }
                    subscriber.onCompleted();
                }))
                .single()
                .doOnError(throwable -> {
                    if (throwable instanceof IllegalArgumentException || throwable instanceof NoSuchElementException) {
                        Lc.assertion(new ShouldNotHappenException("Updates during loading not supported."
                                + " NewerItemsLoader should emit only one result.", throwable));
                    }
                })
                .doOnNext(loadedItems -> onNewerItemsLoaded(loadedItems, renew))
                .replay(1)
                .refCount();
    }

    /**
     * Returns if there are new items to load.
     *
     * @return True if there are more items to load.
     */
    public boolean hasNewerItems() {
        return newerItemsCount.getValue() != 0;
    }

    /**
     * Returns {@link Observable} which is providing status of if is there are new items to load or not.
     *
     * @return {@link Observable} of more items availability status.
     */
    @NonNull
    public Observable<Boolean> observeHasNewerItems() {
        return newerItemsCount.map(count -> count != 0).distinctUntilChanged();
    }

    /**
     * Returns {@link Observable} which is providing count of new items to load.
     *
     * @return {@link Observable} of new items availability status.
     */
    @NonNull
    public Observable<Integer> observeNewerItemsCount() {
        return newerItemsCount.distinctUntilChanged();
    }

    @Override
    protected void onItemsLoaded(@NonNull final TLoadedItems loadedItems, final int insertPosition, final boolean reset) {
        super.onItemsLoaded(loadedItems, insertPosition, reset);
        if (newerReference == null) {
            updateNewerReference(loadedItems);
        }
    }

    /**
     * Calls when newer items part loaded.
     *
     * @param loadedItems Loaded items;
     * @param renew       Flag indicates is it loading just to load some new items (false) or to load totally new items (true).
     */
    protected void onNewerItemsLoaded(@NonNull final TLoadedItems loadedItems, final boolean renew) {
        onItemsLoaded(loadedItems, 0, renew);
        updateNewerReference(loadedItems);
    }

    @Override
    protected void resetState() {
        super.resetState();
        newerReference = null;
        newerItemsCount.onNext(LoadedItems.UNKNOWN_ITEMS_COUNT);
    }

    /**
     * Returns {@link Observable} that will load newer items by count returned by last loaded items part.
     *
     * @return {@link Observable} to load newer items.
     */
    @NonNull
    public Observable<TLoadedItems> loadNewer() {
        return loadingNewerObservable;
    }

    /**
     * Returns {@link Observable} that will load some newer limited by maximum pages loading results.
     *
     * @param maxPageDeep Limit to load pages;
     * @return Returns {@link Observable} to limited load newer items.
     */
    @NonNull
    public Observable<TLoadedItems> loadNewest(final int maxPageDeep) {
        return loadingNewerObservable
                .doOnNext(loadedItems -> {
                    if (loadedItems.getNewerItemsCount() != 0) {
                        throw OnErrorThrowable.from(new NotLoadedYetException());
                    }
                })
                .retry((number, throwable) -> number <= maxPageDeep && throwable instanceof NotLoadedYetException)
                .last();
    }

    /**
     * Returns {@link Observable} that tries to load some newer items even if there are no info about count of them.
     *
     * @return {@link Observable} to load newer items.
     */
    @NonNull
    public Observable<TLoadedItems> renew() {
        return loadingNewestObservable;
    }

    private void updateNewerReference(@NonNull final TLoadedItems loadedItems) {
        if (loadedItems.getNewerReference() != null) {
            newerReference = loadedItems.getNewerReference();
        }
        newerItemsCount.onNext(loadedItems.getNewerItemsCount());
    }

}
