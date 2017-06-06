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

package ru.touchin.roboswag.core.observables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.utils.Optional;
import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 24/03/2016.
 * Variant of {@link BaseChangeable} which is allows to set nullable values.
 * Needed to separate non-null Changeable from nullable Changeable.
 */
//COMPATIBILITY NOTE: in RxJava2 it should extends BaseChangeable<T, Optional<T>>
public class Changeable<T> extends BaseChangeable<T, T> {

    public Changeable(@Nullable final T defaultValue) {
        super(defaultValue);
    }

    /**
     * Returns {@link Observable} which is emits current value and then emitting changes of current value.
     *
     * @return Current value {@link Observable}.
     */
    @NonNull
    @Override
    //COMPATIBILITY NOTE: in RxJava2 it should be Observable<Optional<T>>
    public Observable<T> observe() {
        return observeOptionalValue().map(Optional::get);
    }

}