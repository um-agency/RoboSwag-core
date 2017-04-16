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

import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 24/03/2016.
 * Variant of {@link BaseChangeable} which is allows to set only non-null values.
 * Needed to separate non-null Changeable from nullable Changeable.
 */
public class NonNullChangeable<T> extends BaseChangeable<T, T> {

    public NonNullChangeable(@NonNull final T defaultValue) {
        super(defaultValue);
        if (defaultValue == null) {
            throw new ShouldNotHappenException();
        }
    }

    @NonNull
    @Override
    public T get() {
        final T value = super.get();
        if (value == null) {
            throw new ShouldNotHappenException();
        }
        return value;
    }

    @Override
    public void set(@NonNull final T value) {
        if (value == null) {
            Lc.assertion("value is null");
            return;
        }
        super.set(value);
    }

    /**
     * Returns {@link Observable} which is emits current value and then emitting changes of current value.
     *
     * @return Current value {@link Observable}.
     */
    @NonNull
    @Override
    public Observable<T> observe() {
        return observeOptionalValue()
                .map(optional -> {
                    if (optional.get() == null) {
                        throw new ShouldNotHappenException();
                    }
                    return optional.get();
                });
    }

}