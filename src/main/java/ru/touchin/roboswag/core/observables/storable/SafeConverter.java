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

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * Interface that is providing logic to convert value from specific type to type allowed to store in {@link Store} object and back.
 * The only difference is that it is not throwing exceptions during conversion. It is safe to convert objects with it.
 *
 * @param <TObject>      Type of original objects;
 * @param <TStoreObject> Type of objects in store.
 */
public interface SafeConverter<TObject, TStoreObject> extends Converter<TObject, TStoreObject> {

    @Nullable
    @Override
    TStoreObject toStoreObject(@NonNull Class<TObject> objectClass, @NonNull Class<TStoreObject> storeObjectClass, @Nullable TObject object);

    @Nullable
    @Override
    TObject toObject(@NonNull Class<TObject> objectClass, @NonNull Class<TStoreObject> storeObjectClass, @Nullable TStoreObject storeObject);

}
