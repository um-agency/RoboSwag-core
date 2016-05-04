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

package ru.touchin.roboswag.core.data.storable.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.storable.Migration;
import ru.touchin.roboswag.core.data.storable.SafeConverter;
import ru.touchin.roboswag.core.data.storable.SafeStore;
import ru.touchin.roboswag.core.data.storable.Storable;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 03/05/16.
 * TODO: description
 */
public class SafeStorable<TKey, TObject, TStoreObject>
        extends Storable<TKey, TObject, TStoreObject> {

    protected SafeStorable(@NonNull final TKey key,
                           @NonNull final Class<TObject> objectClass,
                           @NonNull final Class<TStoreObject> storeObjectClass,
                           @NonNull final SafeStore<TKey, TStoreObject> store,
                           @NonNull final SafeConverter<TObject, TStoreObject> converter,
                           final boolean cloneOnGet,
                           @Nullable final Migration<TKey> migration,
                           @Nullable final TObject defaultValue) {
        super(key, objectClass, storeObjectClass, store, converter, cloneOnGet, migration, defaultValue);
    }

    @Nullable
    @Override
    public TObject get() {
        try {
            return super.get();
        } catch (final Exception exception) {
            Lc.assertion(exception);
            return getDefaultValue();
        }
    }

    @Override
    public void set(@Nullable final TObject value) {
        try {
            super.set(value);
        } catch (final Exception exception) {
            Lc.assertion(exception);
        }
    }

    public static class Builder<TKey, TObject, TStoreObject> extends Storable.BaseBuilder<TKey, TObject, TStoreObject> {

        public Builder(@NonNull final Storable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        @NonNull
        public SafeMigratableStorable.Builder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
            setMigrationInternal(migration);
            return new SafeMigratableStorable.Builder<>(this);
        }

        @NonNull
        public NonNullSafeStorable.Builder<TKey, TObject, TStoreObject> setDefaultValue(@NonNull final TObject defaultValue) {
            setDefaultValueInternal(defaultValue);
            return new NonNullSafeStorable.Builder<>(this);
        }

        @NonNull
        public SafeStorable<TKey, TObject, TStoreObject> build() {
            if (getStoreObjectClass() == null || !(getStore() instanceof SafeStore) || !(getConverter() instanceof SafeConverter)) {
                throw new ShouldNotHappenException();
            }
            return new SafeStorable<>(key, objectClass, getStoreObjectClass(),
                    (SafeStore<TKey, TStoreObject>) getStore(), (SafeConverter<TObject, TStoreObject>) getConverter(),
                    cloneOnGet, getMigration(), getDefaultValue());
        }

    }

}
