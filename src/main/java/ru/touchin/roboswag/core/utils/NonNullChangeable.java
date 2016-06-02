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

package ru.touchin.roboswag.core.utils;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 24/03/16.
 */
public class NonNullChangeable<T extends Serializable> extends Changeable<T> {

    private static final long serialVersionUID = 1L;

    private NonNullChangeable() {
        super();
        //for serializer
    }

    public NonNullChangeable(@NonNull final T defaultValue) {
        super(defaultValue);
    }

    @NonNull
    @Override
    public T get() {
        return super.get();
    }

    @SuppressWarnings("PMD.UselessOverridingMethod")
    //UselessOverridingMethod: it's just needed to force @NonNull annotation
    @Override
    public void set(@NonNull final T value) {
        super.set(value);
    }

}