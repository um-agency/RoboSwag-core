/*
 *  Copyright (c) 2017 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

package ru.touchin.roboswag.core.observables.collections.changes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Functional interface for calculating change payload between two items same type.
 * Payload calculating when items are same {@link SameItemsPredicate}, but content different.
 */
public interface ChangePayloadProducer<TItem> {


    /**
     * Calculate change payload between two items.
     *
     * @param item1 First item;
     * @param item2 Second item;
     * @return Object that represents minimal changes between two items.
     */
    @Nullable
    Object getChangePayload(@NonNull TItem item1, @NonNull TItem item2);

}
