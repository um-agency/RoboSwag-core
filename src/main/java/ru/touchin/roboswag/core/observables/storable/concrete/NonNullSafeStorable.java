/*
 *  Copyright (c) 2016 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

package ru.touchin.roboswag.core.observables.storable.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.builders.NonNullSafeMigratableStorableBuilder;
import ru.touchin.roboswag.core.observables.storable.builders.NonNullSafeStorableBuilder;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * {@link Storable} that should not throw exceptions on set or get and return not null value on get.
 * If this rules are violated then it will throw {@link ShouldNotHappenException}.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class NonNullSafeStorable<TKey, TObject, TStoreObject> extends Storable<TKey, TObject, TStoreObject> {

    public NonNullSafeStorable(@NonNull final NonNullSafeStorableBuilder<TKey, TObject, TStoreObject> builderCore) {
        super(builderCore);
    }

    public NonNullSafeStorable(@NonNull final NonNullSafeMigratableStorableBuilder<TKey, TObject, TStoreObject> builderCore) {
        super(builderCore);
    }

    @NonNull
    @Override
    public TObject getSync() {
        final TObject result;
        try {
            result = super.getSync();
        } catch (final Exception exception) {
            throw new ShouldNotHappenException(exception);
        }
        if (result == null) {
            throw new ShouldNotHappenException();
        }
        return result;
    }

    @Override
    public void setSync(@Nullable final TObject newValue) {
        try {
            super.setSync(newValue);
        } catch (final Exception exception) {
            throw new ShouldNotHappenException(exception);
        }
    }

}
