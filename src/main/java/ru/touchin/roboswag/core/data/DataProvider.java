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

package ru.touchin.roboswag.core.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.touchin.roboswag.core.log.Lc;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 16/03/16.
 * TODO: description
 */
public class DataProvider<T> {

    @NonNull
    private final String key;
    private final long expirationPeriod;
    @Nullable
    private final MemoryCache memoryCache;
    @Nullable
    private final DiskCache diskCache;
    @NonNull
    private final Observable<T> actualDataGetter;

    @NonNull
    private final Observable<DataEntry<T>> cacheGetObservableExpired;
    @NonNull
    private final Observable<DataEntry<T>> cacheGetObservableNonExpired;
    @NonNull
    private final Observable<DataEntry<T>> actualDataObservable;
    @NonNull
    private final PublishSubject<Void> reloadEvent = PublishSubject.create();

    public DataProvider(@NonNull final String key, final long expirationPeriod,
                        @Nullable final MemoryCache memoryCache, @Nullable final DiskCache diskCache,
                        @NonNull final Observable<T> actualDataGetter) {
        this.key = key;
        this.expirationPeriod = expirationPeriod;
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
        this.actualDataGetter = actualDataGetter;

        cacheGetObservableExpired = createCacheGetObservable(true);
        cacheGetObservableNonExpired = createCacheGetObservable(false);
        actualDataObservable = createActualDataObservable();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private Observable<DataEntry<T>> createCacheGetObservable(final boolean allowExpired) {
        return Observable
                .<CacheEntry>create(subscriber -> {
                    subscriber.onNext(memoryCache != null ? memoryCache.get(key) : null);
                    subscriber.onCompleted();
                })
                .switchMap(memoryCacheEntry -> getDiskCacheObservable(allowExpired, memoryCacheEntry))
                .switchMap(cacheEntry -> cacheEntry != null && (allowExpired || !cacheEntry.isExpired(expirationPeriod))
                        ? Observable.just(new DataEntry<>(cacheEntry.getCachedTime(), expirationPeriod, (T) cacheEntry.getData()))
                        : Observable.empty())
                .onErrorResumeNext(throwable -> {
                    Lc.assertion(throwable);
                    return Observable.empty();
                })
                .replay(1)
                .refCount();
    }

    @NonNull
    private Observable<CacheEntry> getDiskCacheObservable(final boolean allowExpired, @Nullable final CacheEntry memoryCacheEntry) {
        if (memoryCacheEntry == null) {
            return diskCache != null ? diskCache.get(key).first() : Observable.empty();
        }
        if (allowExpired || !memoryCacheEntry.isExpired(expirationPeriod)) {
            return Observable.just(memoryCacheEntry);
        }
        return Observable.empty();
    }

    @NonNull
    private Observable<DataEntry<T>> createActualDataObservable() {
        return Observable.concat(Observable.just(null), reloadEvent)
                .switchMap(ignored -> actualDataGetter
                        .doOnNext(data -> {
                            if (memoryCache != null) {
                                memoryCache.put(key, data);
                            }
                            if (diskCache != null) {
                                diskCache.put(key, data);
                            }
                        })
                        .map(data -> new DataEntry<>(new Date().getTime(), expirationPeriod, data)))
                .onErrorReturn(DataEntry::new)
                .replay(1)
                .refCount()
                .doOnSubscribe(() -> reloadEvent.onNext(null));
    }

    @NonNull
    public Observable<DataEntry<T>> getCachedData(final boolean allowExpired) {
        return allowExpired ? cacheGetObservableExpired : cacheGetObservableNonExpired;
    }

    @NonNull
    public Observable<DataEntry<T>> observeActualData() {
        return actualDataObservable;
    }

}
