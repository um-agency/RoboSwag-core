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

package ru.touchin.roboswag.core.utils.pairs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import ru.touchin.roboswag.core.utils.ObjectUtils;

/**
 * Created by Ilia Kurtov on 17/01/2017.
 * Pair that needed for saving in state because it implements Serializable interface.
 * Both arguments are not null.
 * Note that if you want to save this pair in state, you need make TFirst and TSecond Serializable too.
 *
 * @param <TFirst> type of the first nonnull argument.
 * @param <TSecond> type of the second nonnull argument.
 */
public class NonNullPair<TFirst, TSecond> implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    private final TFirst first;
    @NonNull
    private final TSecond second;

    public NonNullPair(@NonNull final TFirst first, @NonNull final TSecond second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Get first argument of this pair. It is always not null.
     */
    @NonNull
    public TFirst getFirst() {
        return first;
    }

    /**
     * Get second argument of this pair. It is always not null.
     */
    @NonNull
    public TSecond getSecond() {
        return second;
    }

    @Override
    public boolean equals(@Nullable final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final NonNullPair<?, ?> that = (NonNullPair<?, ?>) object;

        return ObjectUtils.equals(first, that.getFirst()) && ObjectUtils.equals(second, that.getSecond());
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(first, second);
    }

}