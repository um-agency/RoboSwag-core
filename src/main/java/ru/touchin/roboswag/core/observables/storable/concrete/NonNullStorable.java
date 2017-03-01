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

import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.builders.NonNullStorableBuilder;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * {@link Storable} that should return not null value on get.
 * If this rule is violated then it will throw {@link ShouldNotHappenException}.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class NonNullStorable<TKey, TObject, TStoreObject> extends Storable<TKey, TObject, TStoreObject> {

    public NonNullStorable(@NonNull final NonNullStorableBuilder<TKey, TObject, TStoreObject> builderCore) {
        super(builderCore);
    }

    @NonNull
    @Override
    public TObject getSync() throws Throwable {
        final TObject result = super.getSync();
        if (result == null) {
            throw new ShouldNotHappenException();
        }
        return result;
    }

}
