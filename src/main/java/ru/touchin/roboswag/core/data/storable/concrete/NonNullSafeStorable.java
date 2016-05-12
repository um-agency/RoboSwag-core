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
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ObjectUtils;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 03/05/16.
 * TODO: description
 */
@SuppressWarnings("CPD-START")
//TODO: ?
public class NonNullSafeStorable<TKey, TObject, TStoreObject>
        extends NonNullStorable<TKey, TObject, TStoreObject> {

    protected NonNullSafeStorable(@NonNull final BaseBuilder<TKey, TObject, TStoreObject> builder) {
        super(builder);
    }

    @NonNull
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

    @SuppressWarnings("CPD-START")
    //CPD: yes builders have copy-pasted code
    public static class Builder<TKey, TObject, TStoreObject> extends BaseBuilder<TKey, TObject, TStoreObject> {

        public Builder(@NonNull final NonNullStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        public Builder(@NonNull final SafeStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        @NonNull
        @Override
        public TObject getDefaultValue() {
            return ObjectUtils.getNonNull(super::getDefaultValue);
        }

        @NonNull
        public NonNullSafeMigratableStorable.Builder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
            setMigrationInternal(migration);
            return new NonNullSafeMigratableStorable.Builder<>(this);
        }

        @NonNull
        public NonNullSafeStorable<TKey, TObject, TStoreObject> build() {
            if (!(getStore() instanceof SafeStore) || !(getConverter() instanceof SafeConverter)) {
                throw new ShouldNotHappenException();
            }
            return new NonNullSafeStorable<>(this);
        }

    }

}
