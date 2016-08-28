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

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * Object represents loaded items with reference to load other parts and info of are there more items to load or not.
 *
 * @param <TItem>           Type of items to load;
 * @param <TReference>      Type of reference to load other parts of items;
 * @param <TNewerReference> Type of reference to load newer parts of items.
 */
public interface LoadedRenewableItems<TItem, TReference, TNewerReference> extends LoadedItems<TItem, TReference> {

    /**
     * Returns count of new items other than loaded.
     *
     * @return Count of new items other than loaded.
     */
    int getNewerItemsCount();

    /**
     * Returns reference to load newer items from this loaded part.
     *
     * @return Reference to load newer items.
     */
    @Nullable
    TNewerReference getNewerReference();

}
