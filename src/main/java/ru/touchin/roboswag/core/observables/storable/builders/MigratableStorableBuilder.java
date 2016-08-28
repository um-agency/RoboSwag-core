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

package ru.touchin.roboswag.core.observables.storable.builders;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.core.observables.storable.Converter;
import ru.touchin.roboswag.core.observables.storable.Migration;
import ru.touchin.roboswag.core.observables.storable.SafeConverter;
import ru.touchin.roboswag.core.observables.storable.SafeStore;
import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.Store;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 15/05/2016.
 * Builder that is already contains not null migration.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class MigratableStorableBuilder<TKey, TObject, TStoreObject> extends Storable.BuilderCore<TKey, TObject, TStoreObject> {

    public MigratableStorableBuilder(@NonNull final Storable.Builder<TKey, TObject, TStoreObject> sourceBuilder,
                                     @NonNull final Migration<TKey> migration) {
        super(sourceBuilder);
        setMigrationInternal(migration);
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
    public MigratableStorableBuilder<TKey, TObject, TStoreObject> setStore(@NonNull final Class<TStoreObject> storeObjectClass,
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
    public SafeMigratableStorableBuilder<TKey, TObject, TStoreObject> setSafeStore(
            @NonNull final Class<TStoreObject> storeObjectClass,
            @NonNull final SafeStore<TKey, TStoreObject> store,
            @NonNull final SafeConverter<TObject, TStoreObject> converter) {
        setStoreInternal(storeObjectClass, store, converter);
        return new SafeMigratableStorableBuilder<>(this);
    }

    /**
     * Sets value which will be returned instead of null.
     *
     * @param defaultValue Default value;
     * @return Builder that allows to specify other fields.
     */
    @NonNull
    public NonNullMigratableStorableBuilder<TKey, TObject, TStoreObject> setDefaultValue(@NonNull final TObject defaultValue) {
        setDefaultValueInternal(defaultValue);
        return new NonNullMigratableStorableBuilder<>(this);
    }

    /**
     * Building {@link Storable} object.
     *
     * @return New {@link Storable}.
     */
    @NonNull
    public Storable<TKey, TObject, TStoreObject> build() {
        if (getMigration() == null) {
            throw new ShouldNotHappenException();
        }
        return new Storable<>(this);
    }

}