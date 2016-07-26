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
import java.util.List;
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
    }

    @NonNull
    private Observable<TLoadedItems> createLoadingNewerObservable(
            @NonNull final NewerItemsLoader<TItem, TReference, TNewerReference, TLoadedItems> newerItemsLoader,
            final boolean renew) {
        return Observable
                .<TLoadedItems>switchOnNext(Observable.create(subscriber -> {
                    if (!renew) {
                        subscriber.onNext(newerItemsLoader.load(new NewerLoadRequest<>(newerReference, newerItemsCount.getValue())));
                    } else {
                        subscriber.onNext(newerItemsLoader.load(new NewerLoadRequest<>(null, LoadedItems.UNKNOWN_ITEMS_COUNT)));
                    }
                    subscriber.onCompleted();
                }))
                .subscribeOn(Schedulers.io())
                .single()
                .doOnError(throwable -> {
                    if ((throwable instanceof IllegalArgumentException)
                            || (throwable instanceof NoSuchElementException)) {
                        Lc.assertion(new ShouldNotHappenException("Updates during loading not supported." +
                                " NewerItemsLoader should emit only one result.",
                                throwable));
                    }
                })
                .observeOn(getLoaderScheduler())
                .doOnNext(loadedItems -> {
                    if (getInnerList().isEmpty()) {
                        onItemsLoaded(loadedItems, false);
                    } else {
                        onNewerItemsLoaded(loadedItems, renew);
                    }
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
    protected void onItemsLoaded(@NonNull final TLoadedItems loadedItems, final boolean reset) {
        super.onItemsLoaded(loadedItems, reset);
        if (newerReference == null) {
            updateNewerReference(loadedItems);
        }
    }

    protected void onNewerItemsLoaded(@NonNull final TLoadedItems loadedItems, final boolean reset) {
        final List<TItem> items = new ArrayList<>(loadedItems.getItems());
        if (!reset) {
            if (isRemoveDuplicates()) {
                removeDuplicatesFromList(items);
            }
            getInnerList().addAll(0, items);
        } else {
            getInnerList().set(items);
        }
        updateNewerReference(loadedItems);
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
                .retryWhen(attempts -> attempts
                        .zipWith(Observable.range(1, maxPageDeep),
                                (throwable, integer) -> {
                                    if (integer == maxPageDeep) {
                                        throw OnErrorThrowable.from(new TooMuchNewerLoadsException());
                                    }
                                    return throwable;
                                })
                        .switchMap(throwable -> throwable instanceof NotLoadedYetException ? Observable.just(null) : Observable.error(throwable)))
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

    protected static class TooMuchNewerLoadsException extends Exception {
    }

}
