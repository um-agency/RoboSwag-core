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

package org.roboswag.core.utils;

import android.support.annotation.NonNull;

import rx.functions.Func0;

public class ThreadLocalValue<T> extends ThreadLocal<T> {

    @NonNull
    private final Func0<T> creator;

    public ThreadLocalValue(@NonNull final Func0<T> creator) {
        super();
        this.creator = creator;
    }

    @Override
    protected T initialValue() {
        return creator.call();
    }

}