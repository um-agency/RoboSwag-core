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
import ru.touchin.roboswag.core.observables.storable.SafeConverter;
import ru.touchin.roboswag.core.observables.storable.SafeStore;
import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.Store;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 15/05/2016.
 * Builder that is already contains not null migration and default value.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class NonNullMigratableStorableBuilder<TKey, TObject, TStoreObject> extends Storable.BuilderCore<TKey, TObject, TStoreObject> {

    public NonNullMigratableStorableBuilder(@NonNull final NonNullStorableBuilder<TKey, TObject, TStoreObject> sourceBuilder) {
        super(sourceBuilder);
    }

    public NonNullMigratableStorableBuilder(@NonNull final MigratableStorableBuilder<TKey, TObject, TStoreObject> sourceBuilder) {
        super(sourceBuilder);
    }

    @NonNull
    public NonNullMigratableStorableBuilder<TKey, TObject, TStoreObject> setStore(@NonNull final Class<TStoreObject> storeObjectClass,
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
    public NonNullSafeMigratableStorableBuilder<TKey, TObject, TStoreObject> setSafeStore(
            @NonNull final Class<TStoreObject> storeObjectClass,
            @NonNull final SafeStore<TKey, TStoreObject> store,
            @NonNull final SafeConverter<TObject, TStoreObject> converter) {
        setStoreInternal(storeObjectClass, store, converter);
        return new NonNullSafeMigratableStorableBuilder<>(this);
    }

    /**
     * Building {@link Storable} object.
     *
     * @return New {@link Storable}.
     */
    @NonNull
    public Storable<TKey, TObject, TStoreObject> build() {
        if (getDefaultValue() == null) {
            throw new ShouldNotHappenException();
        }
        if (getMigration() == null) {
            throw new ShouldNotHappenException();
        }
        return new Storable<>(this);
    }

}