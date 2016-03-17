package ru.touchin.roboswag.core.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.touchin.roboswag.core.log.Lc;
import rx.Observable;
import rx.subjects.BehaviorSubject;

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
    private final BehaviorSubject<Void> reloadEvent = BehaviorSubject.create((Void) null);

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
                .switchMap(memoryCacheEntry -> {
                    if (memoryCacheEntry == null) {
                        return diskCache != null ? diskCache.get(key).first() : Observable.empty();
                    } else if (allowExpired || !memoryCacheEntry.isExpired(expirationPeriod)) {
                        return Observable.just(memoryCacheEntry);
                    } else {
                        return Observable.empty();
                    }
                })
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
    private Observable<DataEntry<T>> createActualDataObservable() {
        return reloadEvent
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
                .replay(1)
                .refCount();
    }

    @NonNull
    public Observable<DataEntry<T>> getCachedData(final boolean allowExpired) {
        return allowExpired ? cacheGetObservableExpired : cacheGetObservableNonExpired;
    }

    @NonNull
    public Observable<DataEntry<T>> observeActualData() {
        return actualDataObservable.onErrorReturn(DataEntry::new);
    }

    public void reload() {
        reloadEvent.onNext(null);
    }

}
