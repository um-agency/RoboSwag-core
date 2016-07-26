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

public class MoreLoadRequest<TReference> {

    @Nullable
    private final TReference reference;
    private final int nextPosition;

    public MoreLoadRequest(@Nullable final TReference reference, final int nextPosition) {
        this.reference = reference;
        this.nextPosition = nextPosition;
    }

    @Nullable
    public TReference getReference() {
        return reference;
    }

    public int getNextPosition() {
        return nextPosition;
    }

}