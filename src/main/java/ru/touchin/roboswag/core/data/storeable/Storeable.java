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

package ru.touchin.roboswag.core.data.storeable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.exceptions.ConversionException;
import ru.touchin.roboswag.core.data.exceptions.MigrationException;
import ru.touchin.roboswag.core.data.exceptions.StoreException;
import ru.touchin.roboswag.core.data.exceptions.ValidationException;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ObjectUtils;
import rx.Observable;
import rx.functions.Actions;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO
 */
public class Storeable<TKey, TObject, TStoreObject> {

    @NonNull
    private final String name;
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
    private final boolean cloneOnGet;
    @Nullable
    private final Migration<TKey> migration;
    @Nullable
    private final Validator<TObject> validator;
    @Nullable
    private final TObject defaultValue;

    @NonNull
    private final PublishSubject<TObject> valueSubject = PublishSubject.create();
    @NonNull
    private final Observable<TObject> valueObservable = Observable
            .<TObject>create(subscriber -> {
                try {
                    subscriber.onNext(get());
                } catch (Exception throwable) {
                    Lc.e(throwable, "Error during get");
                    subscriber.onError(throwable);
                }
                subscriber.onCompleted();
            })
            .subscribeOn(Schedulers.io())
            .concatWith(valueSubject)
            .replay(1)
            .refCount();

    @Nullable
    private CachedValue<TStoreObject> cachedStoreDefaultValue;
    @Nullable
    private CachedValue<TStoreObject> cachedStoreValue;
    @Nullable
    private CachedValue<TObject> cachedValue;

    @SuppressWarnings("PMD.ExcessiveParameterList")
    protected Storeable(@NonNull final String name,
                        @NonNull final TKey key,
                        @NonNull final Class<TObject> objectClass,
                        @NonNull final Class<TStoreObject> storeObjectClass,
                        @NonNull final Store<TKey, TStoreObject> store,
                        @NonNull final Converter<TObject, TStoreObject> converter,
                        final boolean cloneOnGet,
                        @Nullable final Migration<TKey> migration,
                        @Nullable final Validator<TObject> validator,
                        @Nullable final TObject defaultValue) {
        this.name = name;
        this.key = key;
        this.objectClass = objectClass;
        this.storeObjectClass = storeObjectClass;
        this.store = store;
        this.converter = converter;
        this.cloneOnGet = cloneOnGet;
        this.migration = migration;
        this.validator = validator;
        this.defaultValue = defaultValue;

        /*TODO
        if (isInDebugMode && !cloneOnGet) {
            try {
                ObjectUtils.checkIfIsImmutable(objectClass);
            } catch (ObjectIsMutableException throwable) {
                Log.w(LOG_TAG, Log.getStackTraceString(throwable));
            }
        }*/
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public TKey getKey() {
        return key;
    }

    @NonNull
    public Store<TKey, TStoreObject> getStore() {
        return store;
    }

    @NonNull
    public Converter<TObject, TStoreObject> getConverter() {
        return converter;
    }

    @Nullable
    public TObject getDefaultValue() {
        return defaultValue;
    }

    @Nullable
    public Validator<TObject> getValidator() {
        return validator;
    }

    @NonNull
    private CachedValue<TStoreObject> getCachedStoreDefaultValue() throws ConversionException {
        if (cachedStoreDefaultValue == null) {
            cachedStoreDefaultValue = new CachedValue<>(converter.toStoreObject(objectClass, storeObjectClass, defaultValue));
        }
        return cachedStoreDefaultValue;
    }

    @Nullable
    private TStoreObject getStoreValue() throws StoreException, ConversionException, MigrationException {
        synchronized (this) {
            if (cachedStoreValue == null) {
                if (migration != null) {
                    try {
                        migration.migrateToLatestVersion(key);
                    } catch (MigrationException throwable) {
                        Lc.assertion(throwable);
                    }
                }
                final TStoreObject storeObject = store.loadObject(storeObjectClass, key);
                cachedStoreValue = storeObject == null && defaultValue != null
                        ? getCachedStoreDefaultValue()
                        : new CachedValue<>(storeObject);
            }

            return cachedStoreValue.value;
        }
    }

    @Nullable
    private TObject getDirectValue() throws StoreException, ConversionException {
        synchronized (this) {
            if (cachedValue == null) {
                final TStoreObject storeObject = store.loadObject(storeObjectClass, key);
                cachedValue = storeObject == null && defaultValue != null
                        ? new CachedValue<>(defaultValue)
                        : new CachedValue<>(converter.toObject(objectClass, storeObjectClass, storeObject));
            }
            return cachedValue.value;
        }
    }

    @Nullable
    public TObject get() throws StoreException, ConversionException, MigrationException {
        synchronized (this) {
            if (cloneOnGet) {
                final TStoreObject storeValue = getStoreValue();
                return storeValue != null ? converter.toObject(objectClass, storeObjectClass, storeValue) : null;
            }
            return getDirectValue();
        }
    }

    private void updateCachedValue(@Nullable final TObject value, @Nullable final TStoreObject storeObject) throws ConversionException {
        cachedValue = null;
        cachedStoreValue = null;
        if (cloneOnGet) {
            cachedStoreValue = storeObject == null && defaultValue != null
                    ? getCachedStoreDefaultValue()
                    : new CachedValue<>(storeObject);
        } else {
            cachedValue = storeObject == null && defaultValue != null
                    ? new CachedValue<>(defaultValue)
                    : new CachedValue<>(value);
        }
    }

    public void set(@Nullable final TObject value)
            throws ValidationException, ConversionException, StoreException, MigrationException {
        synchronized (this) {
            if (validator != null) {
                validator.validate(value);
            }

            TObject oldValue = null;
            if (!cloneOnGet && cachedValue != null) {
                oldValue = cachedValue.value;
                if (ObjectUtils.equals(oldValue, value)) {
                    return;
                }
            }

            final TStoreObject valueToStore = converter.toStoreObject(objectClass, storeObjectClass, value);
            try {
                final TStoreObject storedValue = getStoreValue();
                if (ObjectUtils.equals(storedValue, valueToStore)) {
                    return;
                }
                if (oldValue == null) {
                    oldValue = converter.toObject(objectClass, storeObjectClass, storedValue);
                }
            } catch (Exception throwable) {
                // some invalid value in store
                Lc.e(throwable, "Can't get current store value");
            }

            store.storeObject(storeObjectClass, key, valueToStore);
            updateCachedValue(value, valueToStore);
            onValueChanged(get(), oldValue);
        }
    }

    public void setAsync(@Nullable final TObject value) {
        setObservable(value).subscribe(Actions.empty(), Lc::assertion);
    }

    @NonNull
    public Observable<?> setObservable(@Nullable final TObject value) {
        return Observable.create(subscriber -> {
            try {
                set(value);
            } catch (Exception throwable) {
                Lc.e(throwable, "Error during set");
                subscriber.onError(throwable);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

    public Observable<TObject> observe() {
        return valueObservable;
    }

    protected void onValueChanged(@Nullable final TObject newValue, final TObject oldValue) {
        valueSubject.onNext(newValue);
        Lc.d("Value changed from '%s' to '%s'", oldValue, newValue);
    }

    private static class CachedValue<T> {

        @Nullable
        private final T value;

        public CachedValue(@Nullable final T value) {
            this.value = value;
        }

    }

}
