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

import ru.touchin.roboswag.core.observables.storable.Migration;
import ru.touchin.roboswag.core.observables.storable.SafeConverter;
import ru.touchin.roboswag.core.observables.storable.SafeStore;
import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.concrete.NonNullSafeStorable;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 15/05/2016.
 * Builder with safe store and converter inside that is already contains not null default value.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class NonNullSafeStorableBuilder<TKey, TObject, TStoreObject> extends Storable.BuilderCore<TKey, TObject, TStoreObject> {

    public NonNullSafeStorableBuilder(@NonNull final NonNullStorableBuilder<TKey, TObject, TStoreObject> sourceBuilder) {
        super(sourceBuilder);
    }

    public NonNullSafeStorableBuilder(@NonNull final SafeStorableBuilder<TKey, TObject, TStoreObject> sourceBuilder) {
        super(sourceBuilder);
    }

    /**
     * Sets specific {@link Migration} to migrate values from specific version to latest version.
     *
     * @param migration Migration;
     * @return Builder that allows to specify other fields.
     */
    @NonNull
    public NonNullSafeMigratableStorableBuilder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
        setMigrationInternal(migration);
        return new NonNullSafeMigratableStorableBuilder<>(this);
    }

    /**
     * Building {@link NonNullSafeStorable} object.
     *
     * @return New {@link NonNullSafeStorable}.
     */
    @NonNull
    public NonNullSafeStorable<TKey, TObject, TStoreObject> build() {
        if (!(getStore() instanceof SafeStore) || !(getConverter() instanceof SafeConverter)) {
            throw new ShouldNotHappenException();
        }
        if (getDefaultValue() == null) {
            throw new ShouldNotHappenException();
        }
        return new NonNullSafeStorable<>(this);
    }

}