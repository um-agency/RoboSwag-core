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

/**
 * Created by Gavriil Sitnikov on 02/06/2016.
 * Request represents request to load new part of items.
 *
 * @param <TNewerReference> Type of reference to load new part of items.
 */
public class NewerLoadRequest<TNewerReference> {

    @Nullable
    private final TNewerReference newerReference;
    private final int newerItemsCount;

    public NewerLoadRequest(@Nullable final TNewerReference newerReference, final int newerItemsCount) {
        this.newerReference = newerReference;
        this.newerItemsCount = newerItemsCount;
    }

    /**
     * Returns reference to be used to load new part of items.
     *
     * @return Reference object.
     */
    @Nullable
    public TNewerReference getNewerReference() {
        return newerReference;
    }

    /**
     * Count of newer items to load.
     *
     * @return Count of newer items to load.
     */
    public int getNewerItemsCount() {
        return newerItemsCount;
    }

    @Override
    public boolean equals(@Nullable final Object object) {
        return object instanceof NewerLoadRequest
                && ObjectUtils.equals(((NewerLoadRequest) object).newerReference, newerReference)
                && ((NewerLoadRequest) object).newerItemsCount == newerItemsCount;
    }

    @Override
    public int hashCode() {
        return newerItemsCount + (newerReference != null ? newerReference.hashCode() : 0);
    }

}