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

import androidx.annotation.NonNull;

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 02/06/2016.
 * Object that is loading new part of items by reference.
 *
 * @param <TItem>           Type of items to be loaded;
 * @param <TNewerReference> Type of reference to be used to load new part of items;
 * @param <TLoadedItems>    Type of loaded items part.
 */
public interface NewerItemsLoader<TItem, TReference, TNewerReference,
        TLoadedItems extends LoadedRenewableItems<TItem, TReference, TNewerReference>> {

    @NonNull
    Observable<TLoadedItems> load(@NonNull final NewerLoadRequest<TNewerReference> newerLoadRequest);

}