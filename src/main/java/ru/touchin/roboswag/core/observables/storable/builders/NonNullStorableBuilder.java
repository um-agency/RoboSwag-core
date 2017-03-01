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
import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.concrete.NonNullStorable;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 15/05/2016.
 * Builder that is already contains not null default value.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class NonNullStorableBuilder<TKey, TObject, TStoreObject> extends Storable.BuilderCore<TKey, TObject, TStoreObject> {

    public NonNullStorableBuilder(@NonNull final Storable.Builder<TKey, TObject, TStoreObject> sourceBuilder,
                                  @NonNull final TObject defaultValue) {
        super(sourceBuilder);
        setDefaultValueInternal(defaultValue);
    }

    /**
     * Sets specific {@link Migration} to migrate values from specific version to latest version.
     *
     * @param migration Migration;
     * @return Builder that allows to specify other fields.
     */
    @NonNull
    public NonNullStorableBuilder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
        setMigrationInternal(migration);
        return this;
    }

    /**
     * Building {@link NonNullStorable} object.
     *
     * @return New {@link NonNullStorable}.
     */
    @NonNull
    public NonNullStorable<TKey, TObject, TStoreObject> build() {
        if (getDefaultValue() == null) {
            throw new ShouldNotHappenException();
        }
        return new NonNullStorable<>(this);
    }

}