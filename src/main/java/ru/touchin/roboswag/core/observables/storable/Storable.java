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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.touchin.roboswag.core.log.LcGroup;
import ru.touchin.roboswag.core.observables.ObservableResult;
import ru.touchin.roboswag.core.observables.OnSubscribeRefCountWithCacheTime;
import ru.touchin.roboswag.core.observables.RxUtils;
import ru.touchin.roboswag.core.observables.storable.builders.MigratableStorableBuilder;
import ru.touchin.roboswag.core.observables.storable.builders.NonNullStorableBuilder;
import ru.touchin.roboswag.core.observables.storable.builders.SafeStorableBuilder;
import ru.touchin.roboswag.core.utils.ObjectUtils;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.Scheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Actions;
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

    private static final long CACHE_TIME = TimeUnit.SECONDS.toMillis(5);

    @NonNull
    private final TKey key;
    @NonNull
    private final Class<TObject> objectClass;
    @NonNull
    private final Class<TStoreObject> storeObjectClass;
    @NonNull
    private final Store<TKey, TStoreObject> store;
    @NonNull
    private final Converter<TObject, TStoreObject> converter;
    @NonNull
    private final PublishSubject<TStoreObject> newStoreValueEvent = PublishSubject.create();
    @NonNull
    private final Observable<TObject> valueObservable;
    @NonNull
    private final Scheduler storeScheduler;

    public Storable(@NonNull final BuilderCore<TKey, TObject, TStoreObject> builderCore) {
        this(builderCore.key, builderCore.objectClass, builderCore.storeObjectClass,
                builderCore.store, builderCore.converter, builderCore.observeStrategy,
                builderCore.migration, builderCore.defaultValue, builderCore.storeScheduler);
    }

    public Storable(@NonNull final TKey key,
                    @NonNull final Class<TObject> objectClass,
                    @Nullable final Class<TStoreObject> storeObjectClass,
                    @Nullable final Store<TKey, TStoreObject> store,
                    @Nullable final Converter<TObject, TStoreObject> converter,
                    @Nullable final ObserveStrategy observeStrategy,
                    @Nullable final Migration<TKey> migration,
                    @Nullable final TObject defaultValue,
                    @Nullable final Scheduler storeScheduler) {
        if (storeObjectClass == null || store == null || converter == null) {
            throw new ShouldNotHappenException();
        }
        this.key = key;
        this.objectClass = objectClass;
        this.storeObjectClass = storeObjectClass;
        this.store = store;
        this.converter = converter;
        final ObserveStrategy nonNullObserveStrategy = observeStrategy != null ? observeStrategy : getDefaultGetStrategy();
        this.storeScheduler = storeScheduler != null ? storeScheduler : Schedulers.from(Executors.newSingleThreadExecutor());
        final Observable<TStoreObject> storeValueObservable
                = createStoreValueObservable(nonNullObserveStrategy, migration, defaultValue);
        valueObservable = createValueObservable(storeValueObservable, nonNullObserveStrategy);
    }

    @NonNull
    private ObserveStrategy getDefaultGetStrategy() {
        if (ObjectUtils.isSimpleClass(objectClass)) {
            return ObserveStrategy.CACHE_ACTUAL_VALUE;
        }
        if (ObjectUtils.isSimpleClass(storeObjectClass)) {
            return ObserveStrategy.CACHE_STORE_VALUE;
        }
        return ObserveStrategy.NO_CACHE;
    }

    @Nullable
    private TStoreObject returnDefaultValueIfNull(@Nullable final TStoreObject storeObject, @Nullable final TObject defaultValue) {
        if (storeObject == null && defaultValue != null) {
            try {
                return converter.toStoreObject(objectClass, storeObjectClass, defaultValue);
            } catch (final Converter.ConversionException exception) {
                STORABLE_LC_GROUP.w(exception, "Exception while converting default value of '%s' from '%s' from store %s",
                        key, defaultValue, store);
                throw OnErrorThrowable.from(exception);
            } catch (final RuntimeException throwable) {
                STORABLE_LC_GROUP.assertion(throwable);
            }
        }
        return storeObject;
    }

    @NonNull
    private Observable<TStoreObject> createStoreValueObservable(@NonNull final ObserveStrategy observeStrategy,
                                                                @Nullable final Migration<TKey> migration,
                                                                @Nullable final TObject defaultValue) {
        final Observable<TStoreObject> result = Observable
                .<TStoreObject>create(subscriber -> {
                    try {
                        if (migration != null) {
                            migration.migrateToLatestVersion(key);
                        }
                        subscriber.onNext(store.loadObject(storeObjectClass, key));
                        subscriber.onCompleted();
                    } catch (final Store.StoreException storeException) {
                        STORABLE_LC_GROUP.w(storeException, "Exception while trying to get value of '%s' from store %s", key, store);
                        subscriber.onError(storeException);
                    } catch (final Migration.MigrationException migrationException) {
                        STORABLE_LC_GROUP.assertion(migrationException);
                        subscriber.onError(migrationException);
                    } catch (final RuntimeException throwable) {
                        STORABLE_LC_GROUP.assertion(throwable);
                    }
                })
                .subscribeOn(storeScheduler)
                .concatWith(newStoreValueEvent)
                .map(storeObject -> returnDefaultValueIfNull(storeObject, defaultValue));
        return observeStrategy == ObserveStrategy.CACHE_STORE_VALUE
                ? Observable.create(new OnSubscribeRefCountWithCacheTime<>(result.replay(1), CACHE_TIME, TimeUnit.MILLISECONDS))
                : result;
    }

    @NonNull
    private Observable<TObject> createValueObservable(@NonNull final Observable<TStoreObject> storeValueObservable,
                                                      @NonNull final ObserveStrategy observeStrategy) {
        final Observable<TObject> result = storeValueObservable
                .map(storeObject -> {
                    try {
                        return converter.toObject(objectClass, storeObjectClass, storeObject);
                    } catch (final Converter.ConversionException exception) {
                        STORABLE_LC_GROUP.w(exception, "Exception while converting value of '%s' from '%s' from store %s",
                                key, storeObject, store);
                        throw OnErrorThrowable.from(exception);
                    } catch (final RuntimeException throwable) {
                        STORABLE_LC_GROUP.assertion(throwable);
                        throw OnErrorThrowable.from(throwable);
                    }
                })
                .subscribeOn(storeScheduler);

        return observeStrategy == ObserveStrategy.CACHE_ACTUAL_VALUE
                ? Observable.create(new OnSubscribeRefCountWithCacheTime<>(result.replay(1), CACHE_TIME, TimeUnit.MILLISECONDS))
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
     * Returns class of actual object.
     *
     * @return Class of actual object.
     */
    @NonNull
    public Class<TObject> getObjectClass() {
        return objectClass;
    }

    /**
     * Returns class of store object.
     *
     * @return Class of store object.
     */
    @NonNull
    public Class<TStoreObject> getStoreObjectClass() {
        return storeObjectClass;
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
    private Observable<?> internalSet(@Nullable final TObject newValue, final boolean checkForEqualityBeforeSet) {
        return (checkForEqualityBeforeSet ? valueObservable.first() : Observable.just(null))
                .switchMap(value -> {
                    if (checkForEqualityBeforeSet && ObjectUtils.equals(value, newValue)) {
                        return Observable.empty();
                    }
                    return Observable
                            .<TStoreObject>create(subscriber -> {
                                try {
                                    final TStoreObject storeObject = converter.toStoreObject(objectClass, storeObjectClass, newValue);
                                    store.storeObject(storeObjectClass, key, storeObject);
                                    newStoreValueEvent.onNext(storeObject);
                                    STORABLE_LC_GROUP.i("Value of '%s' changed from '%s' to '%s'", key, value, newValue);
                                    subscriber.onCompleted();
                                } catch (final Converter.ConversionException conversionException) {
                                    STORABLE_LC_GROUP.w(conversionException, "Exception while converting value of '%s' from '%s' to store object",
                                            key, newValue, store);
                                    subscriber.onError(conversionException);
                                } catch (final Store.StoreException storeException) {
                                    STORABLE_LC_GROUP.w(storeException, "Exception while trying to store value of '%s' to store %s", key, store);
                                    subscriber.onError(storeException);
                                } catch (final RuntimeException throwable) {
                                    STORABLE_LC_GROUP.assertion(throwable);
                                }
                            });
                });
    }

    /**
     * Creates observable which is async setting value to store.
     * It is not checking if stored value equals new value.
     * In result it will be faster to not get value from store and compare but it will emit item to {@link #observe()} subscribers.
     * NOTE: It could emit ONLY completed and errors events. It is not providing onNext event!
     *
     * @param newValue Value to set;
     * @return Observable of setting process.
     */
    @NonNull
    public Observable<?> forceSet(@Nullable final TObject newValue) {
        return internalSet(newValue, false);
    }

    /**
     * Creates observable which is async setting value to store.
     * NOTE: It could emit ONLY completed and errors events. It is not providing onNext event!
     * Errors won't be emitted if {@link #getStore()} implements {@link SafeStore} and {@link #getConverter()} implements {@link SafeConverter}.
     *
     * @param newValue Value to set;
     * @return Observable of setting process.
     */
    @NonNull
    public Observable<?> set(@Nullable final TObject newValue) {
        return internalSet(newValue, true);
    }

    /**
     * Setting value in background. If error emits then it will raise assertion.
     *
     * @param newValue Value to set.
     */
    public void setCalm(@Nullable final TObject newValue) {
        set(newValue).subscribe(Actions.empty(), STORABLE_LC_GROUP::assertion);
    }

    /**
     * Sets value synchronously. You should NOT use this method normally. Use {@link #set(Object)} asynchronously instead.
     *
     * @param newValue Value to set;
     * @throws Store.StoreException          Throws if {@link Store} threw exception during storing;
     * @throws Converter.ConversionException Throws if {@link Converter} threw exception during conversion;
     * @throws Migration.MigrationException  Throws if {@link Migration} threw exception during migration.
     */
    public void setSync(@Nullable final TObject newValue)
            throws Store.StoreException, Converter.ConversionException, Migration.MigrationException {
        final ObservableResult<?> setResult = RxUtils.executeSync(set(newValue));
        checkStorableObservableResult(setResult);
    }

    /**
     * Returns Observable which is emitting item on subscribe and every time when someone have changed value.
     * It could emit next and error events but not completed.
     * Errors won't be emitted if {@link #getStore()} implements {@link SafeStore} and {@link #getConverter()} implements {@link SafeConverter}.
     *
     * @return Returns observable of value.
     */
    @NonNull
    public Observable<TObject> observe() {
        return valueObservable;
    }

    /**
     * Returns Observable which is emitting only one item on subscribe.
     * It could emit next and error events but not completed.
     * Errors won't be emitted if {@link #getStore()} implements {@link SafeStore} and {@link #getConverter()} implements {@link SafeConverter}.
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
     * @throws Store.StoreException          Throws if {@link Store} threw exception during getting from store;
     * @throws Converter.ConversionException Throws if {@link Converter} threw exception during conversion;
     * @throws Migration.MigrationException  Throws if {@link Migration} threw exception during migration.
     */
    @Nullable
    public TObject getSync()
            throws Store.StoreException, Converter.ConversionException, Migration.MigrationException {
        final ObservableResult<TObject> getResult = RxUtils.executeSync(get());
        checkStorableObservableResult(getResult);
        if (getResult.getItems().size() != 1) {
            throw new ShouldNotHappenException();
        }
        return getResult.getItems().get(0);
    }

    private void checkStorableObservableResult(@NonNull final ObservableResult<?> result)
            throws Store.StoreException, Converter.ConversionException, Migration.MigrationException {
        for (final Throwable throwable : result.getErrors()) {
            if (throwable instanceof Store.StoreException) {
                throw (Store.StoreException) throwable;
            }
            if (throwable instanceof Converter.ConversionException) {
                throw (Converter.ConversionException) throwable;
            }
            if (throwable instanceof Migration.MigrationException) {
                throw (Migration.MigrationException) throwable;
            }
        }
    }

    /**
     * Class that is representing strategy of observing item from store.
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
         * Do not use such strategy if object could be big (like byte-array of file).
         * Do not use such strategy if object is mutable because multiple subscribers could then change it's state.
         */
        CACHE_ACTUAL_VALUE

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
        protected final Class<TObject> objectClass;
        @Nullable
        private Class<TStoreObject> storeObjectClass;
        @Nullable
        private Store<TKey, TStoreObject> store;
        @Nullable
        private Converter<TObject, TStoreObject> converter;
        @Nullable
        protected final ObserveStrategy observeStrategy;
        @Nullable
        private Migration<TKey> migration;
        @Nullable
        private TObject defaultValue;
        @Nullable
        protected final Scheduler storeScheduler;

        protected BuilderCore(@NonNull final TKey key,
                              @NonNull final Class<TObject> objectClass,
                              @Nullable final ObserveStrategy observeStrategy,
                              @Nullable final Scheduler storeScheduler) {
            this(key, objectClass, null, null, null, observeStrategy, null, null, storeScheduler);
        }

        protected BuilderCore(@NonNull final BuilderCore<TKey, TObject, TStoreObject> sourceBuilder) {
            this(sourceBuilder.key, sourceBuilder.objectClass, sourceBuilder.storeObjectClass,
                    sourceBuilder.store, sourceBuilder.converter, sourceBuilder.observeStrategy,
                    sourceBuilder.migration, sourceBuilder.defaultValue, sourceBuilder.storeScheduler);
        }

        @SuppressWarnings("CPD-START")
        //CPD: it is ok that builder copy-pasted parent constructor parameters
        private BuilderCore(@NonNull final TKey key,
                            @NonNull final Class<TObject> objectClass,
                            @Nullable final Class<TStoreObject> storeObjectClass,
                            @Nullable final Store<TKey, TStoreObject> store,
                            @Nullable final Converter<TObject, TStoreObject> converter,
                            @Nullable final ObserveStrategy observeStrategy,
                            @Nullable final Migration<TKey> migration,
                            @Nullable final TObject defaultValue,
                            @Nullable final Scheduler storeScheduler) {
            this.key = key;
            this.objectClass = objectClass;
            this.storeObjectClass = storeObjectClass;
            this.store = store;
            this.converter = converter;
            this.observeStrategy = observeStrategy;
            this.migration = migration;
            this.defaultValue = defaultValue;
            this.storeScheduler = storeScheduler;
        }

        @SuppressWarnings("CPD-END")
        @Nullable
        public Class<TStoreObject> getStoreObjectClass() {
            return storeObjectClass;
        }

        /**
         * Returns {@link Store} where store class representation of object is storing.
         *
         * @return Store.
         */
        @Nullable
        public Store<TKey, TStoreObject> getStore() {
            return store;
        }

        protected void setStoreInternal(@NonNull final Class<TStoreObject> storeObjectClass,
                                        @NonNull final Store<TKey, TStoreObject> store,
                                        @NonNull final Converter<TObject, TStoreObject> converter) {
            this.storeObjectClass = storeObjectClass;
            this.store = store;
            this.converter = converter;
        }

        /**
         * Returns {@link Converter} to convert values from store class to actual and back.
         *
         * @return Converter.
         */
        @Nullable
        public Converter<TObject, TStoreObject> getConverter() {
            return converter;
        }

        /**
         * Returns {@link Migration} to migrate values from specific version to latest version.
         *
         * @return Migration.
         */
        @Nullable
        public Migration<TKey> getMigration() {
            return migration;
        }

        protected void setMigrationInternal(@NonNull final Migration<TKey> migration) {
            this.migration = migration;
        }

        /**
         * Returns value which will be returned instead of null.
         *
         * @return Default value.
         */
        @Nullable
        public TObject getDefaultValue() {
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
                       @NonNull final Class<TObject> objectClass) {
            super(key, objectClass, null, null);
        }

        public Builder(@NonNull final TKey key,
                       @NonNull final Class<TObject> objectClass,
                       @NonNull final ObserveStrategy observeStrategy) {
            super(key, objectClass, observeStrategy, null);
        }

        public Builder(@NonNull final TKey key,
                       @NonNull final Class<TObject> objectClass,
                       @NonNull final Scheduler storeScheduler) {
            super(key, objectClass, null, storeScheduler);
        }

        public Builder(@NonNull final TKey key,
                       @NonNull final Class<TObject> objectClass,
                       @NonNull final ObserveStrategy observeStrategy,
                       @NonNull final Scheduler storeScheduler) {
            super(key, objectClass, observeStrategy, storeScheduler);
        }

        /**
         * Sets store and converter.
         *
         * @param storeObjectClass Class of store object,
         * @param store            Store to store objects into;
         * @param converter        Converter to convert values from store class to actual class and back;
         * @return Builder that allows to specify other fields.
         */
        @NonNull
        public Builder<TKey, TObject, TStoreObject> setStore(@NonNull final Class<TStoreObject> storeObjectClass,
                                                             @NonNull final Store<TKey, TStoreObject> store,
                                                             @NonNull final Converter<TObject, TStoreObject> converter) {
            setStoreInternal(storeObjectClass, store, converter);
            return this;
        }

        /**
         * Sets safe store and converter so in such {@link Storable} it is not needed to specify onError action
         * when subscribing to {@link Storable#set(Object)}, {@link Storable#get()} or {@link Storable#observe()} methods.
         *
         * @param storeObjectClass Class of store object,
         * @param store            Safe store that is not throwing exceptions;
         * @param converter        Safe converter that is not throwing exceptions;
         * @return Builder that allows to specify other fields.
         */
        @NonNull
        public SafeStorableBuilder<TKey, TObject, TStoreObject> setSafeStore(@NonNull final Class<TStoreObject> storeObjectClass,
                                                                             @NonNull final SafeStore<TKey, TStoreObject> store,
                                                                             @NonNull final SafeConverter<TObject, TStoreObject> converter) {
            return new SafeStorableBuilder<>(this, storeObjectClass, store, converter);
        }

        /**
         * Sets specific {@link Migration} to migrate values from specific version to latest version.
         *
         * @param migration Migration;
         * @return Builder that allows to specify other fields.
         */
        @NonNull
        public MigratableStorableBuilder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
            return new MigratableStorableBuilder<>(this, migration);
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
