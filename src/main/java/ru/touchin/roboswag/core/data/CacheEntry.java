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

import java.util.Date;

/**
 * Created by Gavriil Sitnikov on 16/03/16.
 * TODO: description
 */
public class CacheEntry {

    @NonNull
    private final String key;
    private final long cachedTime;
    @NonNull
    private final Object data;

    public CacheEntry(@NonNull final String key, final long cachedTime, @NonNull final Object data) {
        this.key = key;
        this.cachedTime = cachedTime;
        this.data = data;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public long getCachedTime() {
        return cachedTime;
    }

    public boolean isExpired(final long expirationPeriod) {
        final long storeTime = new Date().getTime() - cachedTime;
        return storeTime >= 0 && storeTime < expirationPeriod;
    }

    @NonNull
    public Object getData() {
        return data;
    }

}
