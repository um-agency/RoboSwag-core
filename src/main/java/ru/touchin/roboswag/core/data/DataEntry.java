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

package ru.touchin.roboswag.core.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Gavriil Sitnikov on 16/03/16.
 * TODO: description
 */
public class DataEntry<T> {

    private final long providedTime;
    private final long expirationPeriod;
    @Nullable
    private final T data;
    @Nullable
    private final Throwable error;

    public DataEntry(final long providedTime, final long expirationPeriod, @NonNull final T data) {
        this.providedTime = providedTime;
        this.expirationPeriod = expirationPeriod;
        this.data = data;
        this.error = null;
    }

    public DataEntry(@NonNull final Throwable error) {
        this.providedTime = 0;
        this.expirationPeriod = 0;
        this.data = null;
        this.error = error;
    }

    public long getProvidedTime() {
        return providedTime;
    }

    public long getExpirationPeriod() {
        return expirationPeriod;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

}
