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
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
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

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    //TODO
    public LoadingRenewableList(@NonNull final MoreItemsLoader<TItem, TReference, TLoadedItems> moreMoreItemsLoader,
                                @NonNull final NewerItemsLoader<TItem, TReference, TNewerReference, TLoadedItems> newerItemsLoader) {
        super(moreMoreItemsLoader);
        this.loadingNewerObservable = createLoadingNewerObservable(newerItemsLoader, false);
        this.loadingNewestObservable = createLoadingNewerObservable(newerItemsLoader, true);
    }

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    //TODO
    public LoadingRenewableList(@NonNull final MoreItemsLoader<TItem, TReference, TLoadedItems> moreMoreItemsLoader,
                                @NonNull final NewerItemsLoader<TItem, TReference, TNewerReference, TLoadedItems> newerItemsLoader,
                                @Nullable final TLoadedItems initialItems) {
        super(moreMoreItemsLoader, initialItems);
        this.loadingNewerObservable = createLoadingNewerObservable(newerItemsLoader, false);
        this.loadingNewestObservable = createLoadingNewerObservable(newerItemsLoader, true);
    }

    @NonNull
    private Observable<TLoadedItems> waitForInitialLoading(@NonNull final Observable<TLoadedItems> observable) {
        return getLoadingMoreObservable().ignoreElements().concatWith(observable);
    }

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
                        subscriber.onNext(createLoadRequestBasedObservable(this::createActualRequest,
                                loadRequest -> loadRequest.getNewerReference() == null && isEmpty()
                                        ? waitForInitialLoading(newerItemsLoader.load(loadRequest))
                                        : newerItemsLoader.load(loadRequest)));
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
                .doOnNext(loadedItems -> {
                    onItemsLoaded(loadedItems, 0, renew);
                    onNewerItemsLoaded(loadedItems, renew);
                    updateNewerReference(loadedItems);
                })
                .replay(1)
                .refCount();
    }

    public boolean hasNewerItems() {
        return newerItemsCount.getValue() != 0;
    }

    @NonNull
    public Observable<Boolean> observeHasNewerItems() {
        return newerItemsCount.map(count -> count != 0).distinctUntilChanged();
    }

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

    protected void onNewerItemsLoaded(@NonNull final TLoadedItems loadedItems, final boolean renew) {
        // do nothing
    }

    @Override
    protected void resetState() {
        super.resetState();
        newerReference = null;
        newerItemsCount.onNext(LoadedItems.UNKNOWN_ITEMS_COUNT);
    }

    @NonNull
    public Observable<TLoadedItems> loadNewer() {
        return loadingNewerObservable;
    }

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

    @NonNull
    public Observable<TLoadedItems> renew() {
        return loadingNewestObservable;
    }

    private void updateNewerReference(@NonNull final TLoadedItems loadedItems) {
        newerReference = loadedItems.getNewerReference();
        newerItemsCount.onNext(loadedItems.getNewerItemsCount());
    }

}
