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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * Object represents loaded items with reference to load other parts and info of are there more items to load or not.
 *
 * @param <TItem>      Type of items to load;
 * @param <TReference> Type of reference to load other parts of items.
 */
public interface LoadedItems<TItem, TReference> {

    int UNKNOWN_ITEMS_COUNT = -1;

    /**
     * Returns count of items that could be loaded more.
     *
     * @return Count of items to load more or UNKNOWN_ITEMS_COUNT if it's unknown info.
     */
    int getMoreItemsCount();

    /**
     * Returns loaded items.
     *
     * @return Loaded items.
     */
    @NonNull
    Collection<TItem> getItems();

    /**
     * Returns reference that could be used to load other parts of items.
     *
     * @return Reference object.
     */
    @Nullable
    TReference getReference();

}
