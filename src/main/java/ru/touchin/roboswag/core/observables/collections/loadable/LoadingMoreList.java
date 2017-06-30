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
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.observables.collections.ObservableList;
import ru.touchin.roboswag.core.observables.collections.changes.Change;
import ru.touchin.roboswag.core.observables.collections.changes.CollectionChanges;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Scheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * {@link ObservableCollection} which is loading items more and more by paging/limit-offset/reference-based mechanisms.
 * To use this collection {@link MoreItemsLoader} should be created.
 * {@link MoreItemsLoader} is an object to load next block of items by info from previous loaded block (last loaded item/reference etc.).
 * Use {@link #loadItem(int)} and {@link #loadRange(int, int)} to load items asynchronously.
 *
 * @param <TItem>          Type of collection's items;
 * @param <TMoreReference> Type of reference object to help rightly loading next block of items;
 * @param <TLoadedItems>   Type of loading block of items.
 */
public class LoadingMoreList<TItem, TMoreReference, TLoadedItems extends LoadedItems<TItem, TMoreReference>>
        extends ObservableCollection<TItem> {

    private static final int RETRY_LOADING_AFTER_CHANGE_COUNT = 5;

    private static final LoadedItemsFilter<?> DUPLICATES_REMOVER = (collectionObject, loadedItemsObject) ->
            collectionObject.equals(loadedItemsObject) ? FilterAction.REMOVE_FROM_LOADED_ITEMS : FilterAction.DO_NOTHING;

    @NonNull
    private final Scheduler loaderScheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    @NonNull
    private Observable<TLoadedItems> loadingMoreObservable;
    @NonNull
    private final BehaviorSubject<Integer> moreItemsCount = BehaviorSubject.create(LoadedItems.UNKNOWN_ITEMS_COUNT);
    @NonNull
    private final ObservableList<TItem> innerList = new ObservableList<>();
    @Nullable
    private LoadedItemsFilter<TItem> loadedItemsFilter;
    @Nullable
    private TMoreReference moreItemsReference;

    public LoadingMoreList(@NonNull final MoreItemsLoader<TItem, TMoreReference, TLoadedItems> moreMoreItemsLoader) {
        this(moreMoreItemsLoader, null);
    }

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    //ConstructorCallsOverridableMethod: actually it is calling in lambda callback
    public LoadingMoreList(@NonNull final MoreItemsLoader<TItem, TMoreReference, TLoadedItems> moreMoreItemsLoader,
                           @Nullable final LoadedItems<TItem, TMoreReference> initialItems) {
        super();
        this.loadingMoreObservable = Observable
                .switchOnNext(Observable.fromCallable(() -> createLoadRequestBasedObservable(this::createActualRequest, moreMoreItemsLoader::load)))
                .single()
                .doOnError(throwable -> {
                    if (throwable instanceof IllegalArgumentException || throwable instanceof NoSuchElementException) {
                        Lc.assertion(new ShouldNotHappenException("Updates during loading not supported."
                                + " MoreItemsLoader should emit only one result.", throwable));
                    }
                })
                .doOnNext(loadedItems -> onItemsLoaded(loadedItems, size(), false))
                .replay(1)
                .refCount();

        if (initialItems != null) {
            innerOnItemsLoaded(initialItems, 0, false);
        }
    }

    @Nullable
    public TMoreReference getMoreItemsReference() {
        return moreItemsReference;
    }

    @NonNull
    private MoreLoadRequest<TMoreReference> createActualRequest() {
        return new MoreLoadRequest<>(moreItemsReference, Math.max(0, size()));
    }

    @NonNull
    protected <T, TRequest> Observable<T> createLoadRequestBasedObservable(@NonNull final Func0<TRequest> requestCreator,
                                                                           @NonNull final Func1<TRequest, Observable<T>> observableCreator) {
        return Observable
                .fromCallable(requestCreator)
                .switchMap(loadRequest -> observableCreator.call(loadRequest)
                        .subscribeOn(Schedulers.io())
                        .observeOn(loaderScheduler)
                        .doOnNext(ignored -> {
                            if (!requestCreator.call().equals(loadRequest)) {
                                throw OnErrorThrowable.from(new RequestChangedDuringLoadingException());
                            }
                        }))
                .retry((number, throwable) ->
                        number <= RETRY_LOADING_AFTER_CHANGE_COUNT && throwable instanceof RequestChangedDuringLoadingException);
    }

    @NonNull
    protected final Scheduler getLoaderScheduler() {
        return loaderScheduler;
    }

    @NonNull
    @Override
    public Observable<CollectionChanges<TItem>> observeChanges() {
        return innerList.observeChanges();
    }

    @Override
    protected void notifyAboutChanges(@NonNull final List<TItem> insertedItems,
                                      @NonNull final List<TItem> removedItems,
                                      @NonNull final Collection<Change> changes) {
        Lc.assertion("Illegal operation. Modify getInnerList()");
    }

    /**
     * Returns {@link ObservableList} of already loaded items so you can modify it.
     *
     * @return {@link ObservableList} of already loaded items.
     */
    @NonNull
    protected ObservableList<TItem> getInnerList() {
        return innerList;
    }

    /**
     * Returns if there are more items to load.
     *
     * @return True if there are more items to load.
     */
    public boolean hasMoreItems() {
        return moreItemsCount.getValue() != 0;
    }

    /**
     * Returns {@link Observable} which is providing status of if is there are more items to load or not.
     *
     * @return {@link Observable} of more items availability status.
     */
    @NonNull
    public Observable<Boolean> observeHasMoreItems() {
        return moreItemsCount.map(count -> count != 0).distinctUntilChanged();
    }

    /**
     * Returns {@link Observable} which is providing count of more items to load.
     *
     * @return {@link Observable} of more items availability status.
     */
    @NonNull
    public Observable<Integer> observeMoreItemsCount() {
        return moreItemsCount.distinctUntilChanged();
    }

    /**
     * Sets if duplicates (compared by {@link #equals(Object)}) should be removed from loaded part of items right after loading.
     *
     * @param removeDuplicates True if duplicates should be removed.
     */
    @SuppressWarnings("unchecked")
    //unchecked: it's OK as we are using private static filter
    public void setRemoveDuplicates(final boolean removeDuplicates) {
        if (this.loadedItemsFilter != null && this.loadedItemsFilter != DUPLICATES_REMOVER) {
            Lc.assertion("Remove old filter manually first");
            return;
        }
        this.loadedItemsFilter = removeDuplicates ? (LoadedItemsFilter<TItem>) DUPLICATES_REMOVER : null;
    }

    /**
     * Sets specific filter object which will remove items from already loaded part or from new loaded items right after loading.
     *
     * @param loadedItemsFilter {@link LoadedItemsFilter} to make decision of removing items.
     */
    public void setLoadedItemsFilter(@Nullable final LoadedItemsFilter<TItem> loadedItemsFilter) {
        this.loadedItemsFilter = loadedItemsFilter;
    }

    private void innerOnItemsLoaded(@NonNull final LoadedItems<TItem, TMoreReference> loadedItems, final int insertPosition, final boolean reset) {
        final List<TItem> items = new ArrayList<>(loadedItems.getItems());
        final boolean lastPage = reset || insertPosition > size() - 1;
        if (reset) {
            resetState();
            if (insertPosition != 0) {
                Lc.assertion("Wrong insert position " + insertPosition);
            }
            innerList.set(items);
        } else {
            if (this.loadedItemsFilter != null) {
                filterList(items, this.loadedItemsFilter);
            }
            innerList.addAll(insertPosition, items);
        }
        if (lastPage) {
            moreItemsReference = loadedItems.getReference();
            moreItemsCount.onNext(loadedItems.getMoreItemsCount());
        }
    }

    /**
     * Calls when any new items part loaded.
     *
     * @param loadedItems    Loaded items;
     * @param insertPosition Position to insert loaded items;
     * @param reset          Flag to reset previously loaded items or not.
     */
    protected void onItemsLoaded(@NonNull final TLoadedItems loadedItems, final int insertPosition, final boolean reset) {
        innerOnItemsLoaded(loadedItems, insertPosition, reset);
    }

    private void filterList(@NonNull final List<TItem> items, @NonNull final LoadedItemsFilter<TItem> loadedItemsFilter) {
        for (int i = items.size() - 1; i >= 0; i--) {
            for (int j = innerList.size() - 1; j >= 0; j--) {
                final FilterAction filterAction = loadedItemsFilter.decideFilterAction(innerList.get(j), items.get(i));
                if (filterAction == FilterAction.REMOVE_FROM_LOADED_ITEMS) {
                    items.remove(i);
                    break;
                }
                if (filterAction == FilterAction.REMOVE_FROM_COLLECTION) {
                    innerList.remove(j);
                }
                if (filterAction == FilterAction.REPLACE_SOURCE_ITEM_WITH_LOADED) {
                    innerList.update(j, items.get(i));
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

    /**
     * Returns {@link Observable} that is loading new items.
     *
     * @return {@link Observable} that is loading new items.
     */
    @NonNull
    protected Observable<TLoadedItems> getLoadingMoreObservable() {
        return loadingMoreObservable;
    }

    /**
     * Returns {@link Observable} which is loading item by position.
     * It could return null in onNext callback if there is no item to load for such position.
     *
     * @param position Position to load item;
     * @return {@link Observable} to load item.
     */
    @NonNull
    public Observable<TItem> loadItem(final int position) {
        return Observable
                .switchOnNext(Observable
                        .fromCallable(() -> {
                            if (position < size()) {
                                return Observable.just(get(position));
                            } else if (moreItemsCount.getValue() == 0) {
                                return Observable.just((TItem) null);
                            } else {
                                return loadingMoreObservable.switchMap(ignored -> Observable.<TItem>error(new NotLoadedYetException()));
                            }
                        })
                        .subscribeOn(loaderScheduler))
                .retry((number, throwable) -> throwable instanceof NotLoadedYetException);
    }

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

    /**
     * Remove all loaded items and resets collection's state.
     */
    public void reset() {
        innerList.clear();
        resetState();
    }

    /**
     * Remove all loaded items and resets collection's state but sets some initial items.
     *
     * @param initialItems initial items to be set after reset.
     */
    public void reset(@NonNull final TLoadedItems initialItems) {
        onItemsLoaded(initialItems, 0, true);
    }

    protected void resetState() {
        moreItemsReference = null;
        moreItemsCount.onNext(LoadedItems.UNKNOWN_ITEMS_COUNT);
    }

    /**
     * Action to do with some items while new part of items have loaded.
     */
    public enum FilterAction {
        DO_NOTHING,
        REMOVE_FROM_COLLECTION,
        REMOVE_FROM_LOADED_ITEMS,
        REPLACE_SOURCE_ITEM_WITH_LOADED
    }

    /**
     * Class that is representing object to decide what to do with some items from already loaded and newly loaded part.
     * It should remove duplicates or items with changed data.
     *
     * @param <TItem> Type of collection's items.
     */
    public interface LoadedItemsFilter<TItem> {

        /**
         * Returns action to do based on items: do nothing, remove already loaded item or remove newly loaded item.
         *
         * @param collectionObject  Item from collection of already loaded items;
         * @param loadedItemsObject Item from collection of newly loaded items part;
         * @return Action to do with items.
         */
        @NonNull
        FilterAction decideFilterAction(@NonNull TItem collectionObject, @NonNull TItem loadedItemsObject);

    }

    /**
     * Helper exception happens if {@link #loadItem(int)} called with big index and latest loading items part still not reached such item.
     */
    protected static class NotLoadedYetException extends Exception {
    }

    /**
     * Exception happens if loading request changed during loading so loaded items are not actual anymore.
     */
    protected static class RequestChangedDuringLoadingException extends Exception {
    }

}
