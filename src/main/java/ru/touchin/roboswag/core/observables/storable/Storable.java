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

package ru.touchin.roboswag.core.observables.storable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.touchin.roboswag.core.log.LcGroup;
import ru.touchin.roboswag.core.observables.OnSubscribeRefCountWithCacheTime;
import ru.touchin.roboswag.core.observables.storable.builders.NonNullStorableBuilder;
import ru.touchin.roboswag.core.utils.ObjectUtils;
import rx.Completable;
import rx.Observable;
import rx.Scheduler;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * Base class allows to async access to some store.
 * Supports conversion between store and actual value. If it is not needed then use {@link SameTypesConverter}
 * Supports migration from specific version to latest by {@link Migration} object.
 * Allows to set default value which will be returned if actual value is null.
 * Allows to declare specific {@link ObserveStrategy}.
 * Also specific {@link Scheduler} could be specified to not create new scheduler per storable.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class Storable<TKey, TObject, TStoreObject> {

    public static final LcGroup STORABLE_LC_GROUP = new LcGroup("STORABLE");

    private static final long DEFAULT_CACHE_TIME_MILLIS = TimeUnit.SECONDS.toMillis(5);

    @NonNull
    private static ObserveStrategy getDefaultObserveStrategyFor(@NonNull final Type objectType, @NonNull final Type storeObjectType) {
        if (objectType instanceof Class && ObjectUtils.isSimpleClass((Class) objectType)) {
            return ObserveStrategy.CACHE_ACTUAL_VALUE;
        }
        if (objectType instanceof Class && ObjectUtils.isSimpleClass((Class) storeObjectType)) {
            return ObserveStrategy.CACHE_STORE_VALUE;
        }
        return ObserveStrategy.NO_CACHE;
    }

    @NonNull
    private final TKey key;
    @NonNull
    private final Type objectType;
    @NonNull
    private final Type storeObjectType;
    @NonNull
    private final Store<TKey, TStoreObject> store;
    @NonNull
    private final Converter<TObject, TStoreObject> converter;
    @NonNull
    private final PublishSubject<TStoreObject> newStoreValueEvent = PublishSubject.create();
    @NonNull
    private final Observable<TStoreObject> storeValueObservable;
    @NonNull
    private final Observable<TObject> valueObservable;
    @NonNull
    private final Scheduler scheduler;

    public Storable(@NonNull final BuilderCore<TKey, TObject, TStoreObject> builderCore) {
        this(builderCore.key, builderCore.objectType, builderCore.storeObjectType,
                builderCore.store, builderCore.converter, builderCore.observeStrategy,
                builderCore.migration, builderCore.defaultValue, builderCore.storeScheduler, builderCore.cacheTimeMillis);
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    //ExcessiveParameterList: that's why we are using builder to create it
    private Storable(@NonNull final TKey key,
                     @NonNull final Type objectType,
                     @NonNull final Type storeObjectType,
                     @NonNull final Store<TKey, TStoreObject> store,
                     @NonNull final Converter<TObject, TStoreObject> converter,
                     @Nullable final ObserveStrategy observeStrategy,
                     @Nullable final Migration<TKey> migration,
                     @Nullable final TObject defaultValue,
                     @Nullable final Scheduler storeScheduler,
                     final long cacheTimeMillis) {
        this.key = key;
        this.objectType = objectType;
        this.storeObjectType = storeObjectType;
        this.store = store;
        this.converter = converter;
        final ObserveStrategy nonNullObserveStrategy
                = observeStrategy != null ? observeStrategy : getDefaultObserveStrategyFor(objectType, storeObjectType);
        scheduler = storeScheduler != null ? storeScheduler : Schedulers.from(Executors.newSingleThreadExecutor());
        storeValueObservable
                = createStoreValueObservable(nonNullObserveStrategy, migration, defaultValue, cacheTimeMillis);
        valueObservable = createValueObservable(storeValueObservable, nonNullObserveStrategy, cacheTimeMillis);
    }

    @Nullable
    private TStoreObject returnDefaultValueIfNull(@Nullable final TStoreObject storeObject, @Nullable final TObject defaultValue) {
        if (storeObject != null || defaultValue == null) {
            return storeObject;
        }

        try {
            return converter.toStoreObject(objectType, storeObjectType, defaultValue);
        } catch (final Converter.ConversionException exception) {
            STORABLE_LC_GROUP.w(exception, "Exception while converting default value of '%s' from '%s' from store %s",
                    key, defaultValue, store);
            throw OnErrorThrowable.from(exception);
        } catch (final RuntimeException throwable) {
            STORABLE_LC_GROUP.assertion(throwable);
            throw OnErrorThrowable.from(throwable);
        }
    }

    @NonNull
    private Observable<TStoreObject> createStoreValueObservable(@NonNull final ObserveStrategy observeStrategy,
                                                                @Nullable final Migration<TKey> migration,
                                                                @Nullable final TObject defaultValue,
                                                                final long cacheTimeMillis) {
        final Observable<TStoreObject> result = (migration != null
                ? migration.migrateToLatestVersion(key).subscribeOn(scheduler)
                : Completable.complete())
                .andThen(store.loadObject(storeObjectType, key).toObservable().subscribeOn(scheduler))
                .doOnError(throwable -> {
                    if (throwable instanceof RuntimeException) {
                        STORABLE_LC_GROUP.assertion(throwable);
                    } else {
                        STORABLE_LC_GROUP.w(throwable, "Exception while trying to load value of '%s' from store %s", key, store);
                    }
                })
                .concatWith(newStoreValueEvent)
                .map(storeObject -> returnDefaultValueIfNull(storeObject, defaultValue));
        return observeStrategy == ObserveStrategy.CACHE_STORE_VALUE || observeStrategy == ObserveStrategy.CACHE_STORE_AND_ACTUAL_VALUE
                ? Observable.create(new OnSubscribeRefCountWithCacheTime<>(result.replay(1), cacheTimeMillis, TimeUnit.MILLISECONDS))
                : result;
    }

    @NonNull
    private Observable<TObject> createValueObservable(@NonNull final Observable<TStoreObject> storeValueObservable,
                                                      @NonNull final ObserveStrategy observeStrategy,
                                                      final long cacheTimeMillis) {
        final Observable<TObject> result = storeValueObservable
                .switchMap(storeObject -> Observable
                        .fromCallable(() -> converter.toObject(objectType, storeObjectType, storeObject))
                        .subscribeOn(scheduler)
                        .doOnError(throwable -> {
                            if (throwable instanceof RuntimeException) {
                                STORABLE_LC_GROUP.assertion(throwable);
                            } else {
                                STORABLE_LC_GROUP.w(throwable, "Exception while trying to converting value of '%s' from store %s by %s",
                                        key, storeObject, store, converter);
                            }
                        }));
        return observeStrategy == ObserveStrategy.CACHE_ACTUAL_VALUE || observeStrategy == ObserveStrategy.CACHE_STORE_AND_ACTUAL_VALUE
                ? Observable.create(new OnSubscribeRefCountWithCacheTime<>(result.replay(1), cacheTimeMillis, TimeUnit.MILLISECONDS))
                : result;
    }

    /**
     * Returns key of value.
     *
     * @return Unique key.
     */
    @NonNull
    public TKey getKey() {
        return key;
    }

    /**
     * Returns type of actual object.
     *
     * @return Type of actual object.
     */
    @NonNull
    public Type getObjectType() {
        return objectType;
    }

    /**
     * Returns type of store object.
     *
     * @return Type of store object.
     */
    @NonNull
    public Type getStoreObjectType() {
        return storeObjectType;
    }

    /**
     * Returns {@link Store} where store class representation of object is storing.
     *
     * @return Store.
     */
    @NonNull
    public Store<TKey, TStoreObject> getStore() {
        return store;
    }

    /**
     * Returns {@link Converter} to convert values from store class to actual and back.
     *
     * @return Converter.
     */
    @NonNull
    public Converter<TObject, TStoreObject> getConverter() {
        return converter;
    }

    @NonNull
    private Completable internalSet(@Nullable final TObject newValue, final boolean checkForEqualityBeforeSet) {
        return (checkForEqualityBeforeSet ? storeValueObservable.first() : Observable.just(null))
                .switchMap(oldStoreValue -> Observable
                        .fromCallable(() -> converter.toStoreObject(objectType, storeObjectType, newValue))
                        .subscribeOn(scheduler)
                        .switchMap(newStoreValue -> {
                            if (checkForEqualityBeforeSet && ObjectUtils.equals(newStoreValue, oldStoreValue)) {
                                return Observable.empty();
                            }
                            return store.storeObject(storeObjectType, key, newStoreValue)
                                    .doOnCompleted(() -> {
                                        newStoreValueEvent.onNext(newStoreValue);
                                        if (checkForEqualityBeforeSet) {
                                            STORABLE_LC_GROUP.i("Value of '%s' changed from '%s' to '%s'", key, oldStoreValue, newStoreValue);
                                        } else {
                                            STORABLE_LC_GROUP.i("Value of '%s' force changed to '%s'", key, newStoreValue);
                                        }
                                    })
                                    .toObservable();
                        }))
                .doOnError(throwable -> {
                    if (throwable instanceof RuntimeException) {
                        STORABLE_LC_GROUP.assertion(throwable);
                    } else {
                        STORABLE_LC_GROUP.w(throwable, "Exception while trying to store value of '%s' from store %s by %s",
                                key, newValue, store, converter);
                    }
                })
                .toCompletable();
    }

    /**
     * Creates observable which is async setting value to store.
     * It is not checking if stored value equals new value.
     * In result it will be faster to not get value from store and compare but it will emit item to {@link #observe()} subscribers.
     * NOTE: It could emit ONLY completed and errors events. It is not providing onNext event! //TODO: it's Completable :(
     *
     * @param newValue Value to set;
     * @return Observable of setting process.
     */
    @NonNull
    public Observable<?> forceSet(@Nullable final TObject newValue) {
        return internalSet(newValue, false).toObservable();
    }

    /**
     * Creates observable which is async setting value to store.
     * It is checking if stored value equals new value.
     * In result it will take time to get value from store and compare
     * but it won't emit item to {@link #observe()} subscribers if stored value equals new value.
     * NOTE: It could emit ONLY completed and errors events. It is not providing onNext event! //TODO: it's Completable :(
     *
     * @param newValue Value to set;
     * @return Observable of setting process.
     */
    @NonNull
    public Observable<?> set(@Nullable final TObject newValue) {
        return internalSet(newValue, true).toObservable();
    }

    /**
     * Sets value synchronously. You should NOT use this method normally. Use {@link #set(Object)} asynchronously instead.
     *
     * @param newValue Value to set;
     */
    @Deprecated
    //deprecation: it should be used for debug only and in very rare cases.
    public void setSync(@Nullable final TObject newValue) {
        set(newValue).toBlocking().subscribe();
    }

    /**
     * Returns Observable which is emitting item on subscribe and every time when someone have changed value.
     * It could emit next and error events but not completed.
     *
     * @return Returns observable of value.
     */
    @NonNull
    public Observable<TObject> observe() {
        return valueObservable;
    }

    /**
     * Returns Observable which is emitting only one item on subscribe. //TODO: it's Single :(
     * It could emit next and error events but not completed.
     *
     * @return Returns observable of value.
     */
    @NonNull
    public Observable<TObject> get() {
        return valueObservable.first();
    }

    /**
     * Gets value synchronously. You should NOT use this method normally. Use {@link #get()} or {@link #observe()} asynchronously instead.
     *
     * @return Returns value;
     */
    @Deprecated
    //deprecation: it should be used for debug only and in very rare cases.
    @Nullable
    public TObject getSync() {
        return get().toBlocking().first();
    }

    /**
     * Enum that is representing strategy of observing item from store.
     */
    public enum ObserveStrategy {

        /**
         * Not caching value so on every {@link #get()} emit it will get value from {@link #getStore()} and converts it with {@link #getConverter()}.
         */
        NO_CACHE,
        /**
         * Caching only store value so on every {@link #get()} emit it will converts it with {@link #getConverter()}.
         * Do not use such strategy if store object could be big (like byte-array of file).
         */
        CACHE_STORE_VALUE,
        /**
         * Caching value so it won't spend time for getting value from {@link #getStore()} and converts it by {@link #getConverter()}.
         * But it will take time for getting value from {@link #getStore()} to set value.
         * Do not use such strategy if object could be big (like Bitmap or long string).
         * Do not use such strategy if object is mutable because multiple subscribers could then change it's state.
         */
        CACHE_ACTUAL_VALUE,
        /**
         * Caching value so it won't spend time for getting value from {@link #getStore()} and converts it by {@link #getConverter()}.
         * It won't take time or getting value from {@link #getStore()} to set value.
         * Do not use such strategy if store object could be big (like byte-array of file).
         * Do not use such strategy if object could be big (like Bitmap or long string).
         * Do not use such strategy if object is mutable because multiple subscribers could then change it's state.
         */
        CACHE_STORE_AND_ACTUAL_VALUE

    }

    /**
     * Helper class to create various builders.
     *
     * @param <TKey>         Type of key to identify object;
     * @param <TObject>      Type of actual object;
     * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
     */
    public static class BuilderCore<TKey, TObject, TStoreObject> {

        @NonNull
        protected final TKey key;
        @NonNull
        protected final Type objectType;
        @NonNull
        private final Type storeObjectType;
        @NonNull
        private final Store<TKey, TStoreObject> store;
        @NonNull
        private final Converter<TObject, TStoreObject> converter;
        @Nullable
        private ObserveStrategy observeStrategy;
        @Nullable
        private Migration<TKey> migration;
        @Nullable
        private TObject defaultValue;
        @Nullable
        private Scheduler storeScheduler;
        private long cacheTimeMillis;

        protected BuilderCore(@NonNull final TKey key,
                              @NonNull final Type objectType,
                              @NonNull final Type storeObjectType,
                              @NonNull final Store<TKey, TStoreObject> store,
                              @NonNull final Converter<TObject, TStoreObject> converter) {
            this(key, objectType, storeObjectType, store, converter, null, null, null, null, DEFAULT_CACHE_TIME_MILLIS);
        }

        protected BuilderCore(@NonNull final BuilderCore<TKey, TObject, TStoreObject> sourceBuilder) {
            this(sourceBuilder.key, sourceBuilder.objectType, sourceBuilder.storeObjectType,
                    sourceBuilder.store, sourceBuilder.converter, sourceBuilder.observeStrategy,
                    sourceBuilder.migration, sourceBuilder.defaultValue, sourceBuilder.storeScheduler, sourceBuilder.cacheTimeMillis);
        }

        @SuppressWarnings({"PMD.ExcessiveParameterList", "CPD-START"})
        //CPD: it is same code as constructor of Storable
        //ExcessiveParameterList: that's why we are using builder to create it
        private BuilderCore(@NonNull final TKey key,
                            @NonNull final Type objectType,
                            @NonNull final Type storeObjectType,
                            @NonNull final Store<TKey, TStoreObject> store,
                            @NonNull final Converter<TObject, TStoreObject> converter,
                            @Nullable final ObserveStrategy observeStrategy,
                            @Nullable final Migration<TKey> migration,
                            @Nullable final TObject defaultValue,
                            @Nullable final Scheduler storeScheduler,
                            final long cacheTimeMillis) {
            this.key = key;
            this.objectType = objectType;
            this.storeObjectType = storeObjectType;
            this.store = store;
            this.converter = converter;
            this.observeStrategy = observeStrategy;
            this.migration = migration;
            this.defaultValue = defaultValue;
            this.storeScheduler = storeScheduler;
            this.cacheTimeMillis = cacheTimeMillis;
        }

        @SuppressWarnings("CPD-END")
        protected void setStoreSchedulerInternal(@Nullable final Scheduler storeScheduler) {
            this.storeScheduler = storeScheduler;
        }

        protected void setObserveStrategyInternal(@Nullable final ObserveStrategy observeStrategy) {
            this.observeStrategy = observeStrategy;
        }

        protected void setMigrationInternal(@NonNull final Migration<TKey> migration) {
            this.migration = migration;
        }

        protected void setCacheTimeInternal(final long cacheTime, @NonNull final TimeUnit timeUnit) {
            this.cacheTimeMillis = timeUnit.toMillis(cacheTime);
        }

        @Nullable
        protected TObject getDefaultValue() {
            return defaultValue;
        }

        protected void setDefaultValueInternal(@NonNull final TObject defaultValue) {
            this.defaultValue = defaultValue;
        }

    }

    /**
     * Helper class to build {@link Storable}.
     *
     * @param <TKey>         Type of key to identify object;
     * @param <TObject>      Type of actual object;
     * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
     */
    public static class Builder<TKey, TObject, TStoreObject> extends BuilderCore<TKey, TObject, TStoreObject> {

        public Builder(@NonNull final TKey key,
                       @NonNull final Type objectType,
                       @NonNull final Type storeObjectType,
                       @NonNull final Store<TKey, TStoreObject> store,
                       @NonNull final Converter<TObject, TStoreObject> converter) {
            super(key, objectType, storeObjectType, store, converter);
        }

        /**
         * Sets specific {@link Scheduler} to store/load/convert values on it.
         *
         * @param storeScheduler Scheduler;
         * @return Builder that allows to specify other fields.
         */
        @NonNull
        public Builder<TKey, TObject, TStoreObject> setStoreScheduler(@Nullable final Scheduler storeScheduler) {
            setStoreSchedulerInternal(storeScheduler);
            return this;
        }

        /**
         * Sets specific {@link ObserveStrategy} to cache value in memory in specific way.
         *
         * @param observeStrategy ObserveStrategy;
         * @return Builder that allows to specify other fields.
         */
        @NonNull
        public Builder<TKey, TObject, TStoreObject> setObserveStrategy(@Nullable final ObserveStrategy observeStrategy) {
            setObserveStrategyInternal(observeStrategy);
            return this;
        }

        /**
         * Sets cache time for while value that cached by {@link #setObserveStrategy(ObserveStrategy)} will be in memory after everyone unsubscribe.
         * It is important for example for cases when user switches between screens and hide/open app very fast.
         *
         * @param cacheTime Cache time value;
         * @param timeUnit  Cache time units.
         * @return Builder that allows to specify other fields.
         */
        @NonNull
        public Builder<TKey, TObject, TStoreObject> setCacheTime(final long cacheTime, @NonNull final TimeUnit timeUnit) {
            setCacheTimeInternal(cacheTime, timeUnit);
            return this;
        }

        /**
         * Sets specific {@link Migration} to migrate values from specific version to latest version.
         *
         * @param migration Migration;
         * @return Builder that allows to specify other fields.
         */
        @NonNull
        public Builder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
            setMigrationInternal(migration);
            return this;
        }

        /**
         * Sets value which will be returned instead of null.
         *
         * @param defaultValue Default value;
         * @return Builder that allows to specify other fields.
         */
        @NonNull
        public NonNullStorableBuilder<TKey, TObject, TStoreObject> setDefaultValue(@NonNull final TObject defaultValue) {
            return new NonNullStorableBuilder<>(this, defaultValue);
        }

        /**
         * Building {@link Storable} object.
         *
         * @return New {@link Storable}.
         */
        @NonNull
        public Storable<TKey, TObject, TStoreObject> build() {
            return new Storable<>(this);
        }

    }

}
