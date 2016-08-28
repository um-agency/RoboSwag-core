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

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 02/06/2016.
 * Object that is loading next part of items by reference or position.
 *
 * @param <TItem>          Type of items to be loaded;
 * @param <TMoreReference> Type of reference to be used to load next part of items;
 * @param <TLoadedItems>   Type of loaded items part.
 */
public interface MoreItemsLoader<TItem, TMoreReference, TLoadedItems extends LoadedItems<TItem, TMoreReference>> {

    /**
     * Returns {@link Observable} that could load next part of items.
     *
     * @param moreLoadRequest Request with info inside to load next part of items;
     * @return {@link Observable} of loading items.
     */
    @NonNull
    Observable<TLoadedItems> load(@NonNull final MoreLoadRequest<TMoreReference> moreLoadRequest);

}