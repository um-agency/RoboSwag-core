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

package ru.touchin.roboswag.core.observables.storable.builders;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import ru.touchin.roboswag.core.observables.storable.Migration;
import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.concrete.NonNullStorable;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Scheduler;

/**
 * Created by Gavriil Sitnikov on 15/05/2016.
 * Builder that is already contains not null default value.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class NonNullStorableBuilder<TKey, TObject, TStoreObject> extends Storable.BuilderCore<TKey, TObject, TStoreObject> {

    public NonNullStorableBuilder(@NonNull final Storable.Builder<TKey, TObject, TStoreObject> sourceBuilder,
                                  @NonNull final TObject defaultValue) {
        super(sourceBuilder);
        setDefaultValueInternal(defaultValue);
    }

    /**
     * Sets specific {@link Scheduler} to store/load/convert values on it.
     *
     * @param storeScheduler Scheduler;
     * @return Builder that allows to specify other fields.
     */
    @NonNull
    public NonNullStorableBuilder<TKey, TObject, TStoreObject> setStoreScheduler(@Nullable final Scheduler storeScheduler) {
        setStoreSchedulerInternal(storeScheduler);
        return this;
    }

    /**
     * Sets specific {@link Storable.ObserveStrategy} to cache value in memory in specific way.
     *
     * @param observeStrategy ObserveStrategy;
     * @return Builder that allows to specify other fields.
     */
    @NonNull
    public NonNullStorableBuilder<TKey, TObject, TStoreObject> setObserveStrategy(@Nullable final Storable.ObserveStrategy observeStrategy) {
        setObserveStrategyInternal(observeStrategy);
        return this;
    }

    /**
     * Sets cache time for while value that cached by {@link #setObserveStrategy(Storable.ObserveStrategy)}
     * will be in memory after everyone unsubscribe.
     * It is important for example for cases when user switches between screens and hide/open app very fast.
     *
     * @param cacheTime Cache time value;
     * @param timeUnit  Cache time units.
     * @return Builder that allows to specify other fields.
     */
    @NonNull
    public NonNullStorableBuilder<TKey, TObject, TStoreObject> setCacheTime(final long cacheTime, @NonNull final TimeUnit timeUnit) {
        setCacheTimeInternal(cacheTime, timeUnit);
        return this;
    }

    /**
     * Sets specific {@link Migration} to migrate values from specific version to latest version.
     *
     * @param migration Migration;
     * @return Builder that allows to specify other fields.
     */
    @NonNull
    public NonNullStorableBuilder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
        setMigrationInternal(migration);
        return this;
    }

    /**
     * Building {@link NonNullStorable} object.
     *
     * @return New {@link NonNullStorable}.
     */
    @NonNull
    public NonNullStorable<TKey, TObject, TStoreObject> build() {
        if (getDefaultValue() == null) {
            throw new ShouldNotHappenException();
        }
        return new NonNullStorable<>(this);
    }

}