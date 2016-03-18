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

package ru.touchin.roboswag.core.data.storable;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.core.data.storable.exceptions.MigrationException;

/**
 * Created by Gavriil Sitnikov on 05/10/2015.
 * TODO: fill description
 */
public abstract class Migrator<TKey, TOldStoreObject, TNewStoreObject> {

    public static final long DEFAULT_VERSION = -1L;

    @NonNull
    private final Store<TKey, TOldStoreObject> oldStore;
    @NonNull
    private final Store<TKey, TNewStoreObject> newStore;

    public Migrator(@NonNull Store<TKey, TOldStoreObject> oldStore,
                    @NonNull Store<TKey, TNewStoreObject> newStore) {
        this.oldStore = oldStore;
        this.newStore = newStore;
    }

    @NonNull
    public Store<TKey, TOldStoreObject> getOldStore() {
        return oldStore;
    }

    @NonNull
    public Store<TKey, TNewStoreObject> getNewStore() {
        return newStore;
    }

    public abstract boolean supportMigrationFor(long version);

    public long migrate(TKey oldKey, long version) throws MigrationException {
        if (!supportMigrationFor(version)) {
            throw new MigrationException("Version " + version + " not supported by " + this);
        }
        return migrateInternal(oldKey);
    }

    protected abstract long migrateInternal(TKey key) throws MigrationException;

}
