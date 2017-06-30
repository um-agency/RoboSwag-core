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

import java.util.List;

/**
 * Interface that represent changes calculator between two collections.
 */
public interface CollectionsChangesCalculator<TItem> {

    /**
     * Calculate changes between two collection as collection of objects {@link Change}.
     *
     * @return List of changes.
     */
    @NonNull
    List<Change> calculateChanges();

    /**
     * Calculate changes between two collection as collection of inserted items.
     *
     * @return List of inserted item.
     */
    @NonNull
    List<TItem> calculateInsertedItems();

    /**
     * Calculate changes between two collection as collection of removed items.
     *
     * @return List of removed item.
     */
    @NonNull
    List<TItem> calculateRemovedItems();

}
