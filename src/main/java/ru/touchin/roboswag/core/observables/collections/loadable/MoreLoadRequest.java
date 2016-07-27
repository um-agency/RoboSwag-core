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

package ru.touchin.roboswag.core.observables.collections.loadable;

import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.utils.ObjectUtils;

public class MoreLoadRequest<TMoreReference> {

    @Nullable
    private final TMoreReference moreReference;
    private final int nextPosition;

    public MoreLoadRequest(@Nullable final TMoreReference moreReference, final int nextPosition) {
        this.moreReference = moreReference;
        this.nextPosition = nextPosition;
    }

    @Nullable
    public TMoreReference getReference() {
        return moreReference;
    }

    public int getNextPosition() {
        return nextPosition;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof MoreLoadRequest
                && ObjectUtils.equals(((MoreLoadRequest) obj).moreReference, moreReference)
                && ((MoreLoadRequest) obj).nextPosition == nextPosition;
    }

    @Override
    public int hashCode() {
        return nextPosition + (moreReference != null ? moreReference.hashCode() : 0);
    }

}