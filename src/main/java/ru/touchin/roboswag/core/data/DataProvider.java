package ru.touchin.roboswag.core.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.log.Lc;
import rx.Observable;

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
    }

    @SuppressWarnings("unchecked")
    private Observable<DataEntry<T>> createCacheGetObservable(final boolean allowExpired) {
        return Observable
                .<CacheEntry>create(subscriber -> {
                    subscriber.onNext(memoryCache != null ? memoryCache.get(key) : null);
                    subscriber.onCompleted();
                })
                .switchMap(memoryCacheEntry -> {
                    if (memoryCacheEntry == null) {
                        return diskCache != null ? diskCache.get(key) : Observable.just(null);
                    } else if (allowExpired || !memoryCacheEntry.isExpired(expirationPeriod)) {
                        return Observable.just(memoryCacheEntry);
                    } else {
                        return Observable.just(null);
                    }
                })
                .map(cacheEntry -> cacheEntry != null && (allowExpired || !cacheEntry.isExpired(expirationPeriod))
                        ? new DataEntry<>(cacheEntry.getCachedTime(), expirationPeriod, (T) cacheEntry.getData())
                        : null)
                .onErrorReturn(throwable -> {
                    Lc.assertion(throwable);
                    return null;
                })
                .replay(1)
                .refCount();
    }

    @NonNull
    public Observable<DataEntry<T>> getCachedData(final boolean allowExpired) {
        return allowExpired ? cacheGetObservableExpired : cacheGetObservableNonExpired;
    }

    public Observable<DataEntry<T>> observeActualData() {
        return null;/*
                .switchMap(cacheDataEntry -> {
                    final Observable<DataEntry<T>> getter = actualDataGetter
                            .doOnNext(data -> {
                                if (memoryCache != null) {
                                    memoryCache.put(key, data);
                                }
                                if (diskCache != null) {
                                    diskCache.put(key, data);
                                }
                            })
                            .map(data -> new DataEntry<>(new Date().getTime(), expirationPeriod, data));
                    return cacheDataEntry != null ? Observable.just(cacheDataEntry).concatWith(getter) : getter;
                });*/
    }

}
