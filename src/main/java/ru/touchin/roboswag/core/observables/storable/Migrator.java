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

package ru.touchin.roboswag.core.observables.storable;

import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 05/10/2015.
 * Abstract class of objects which are able to migrate some values from one version to another.
 * Also it is able to move objects from one store to another.
 *
 * @param <TKey>            Type of keys of migrating values;
 * @param <TOldStoreObject> Type of values from current store;
 * @param <TNewStoreObject> Type of values from new store. Could be same as {@link TOldStoreObject}.
 */
public abstract class Migrator<TKey, TOldStoreObject, TNewStoreObject> {

    @NonNull
    private final Store<TKey, TOldStoreObject> oldStore;
    @NonNull
    private final Store<TKey, TNewStoreObject> newStore;

    public Migrator(@NonNull final Store<TKey, TOldStoreObject> oldStore,
                    @NonNull final Store<TKey, TNewStoreObject> newStore) {
        this.oldStore = oldStore;
        this.newStore = newStore;
    }

    /**
     * Returns if this migrator can migrate from specific version to some new version.
     *
     * @param version Version to migrate from;
     * @return True if migrator supports migration from this version.
     */
    public abstract boolean supportsMigrationFor(long version);

    /**
     * Returns if specific object with key of specific version could be migrated by this migrator.
     *
     * @param key     Key of object to migrate;
     * @param version Current version of object;
     * @return True if object with such key and version could be migrated;
     * @throws Migration.MigrationException Exception during object migration. Usually it indicates illegal state.
     */
    public boolean canMigrate(@NonNull final TKey key, final long version) throws Migration.MigrationException {
        try {
            return oldStore.contains(key);
        } catch (final Store.StoreException exception) {
            throw new Migration.MigrationException("Version " + version + " not supported by " + this, exception);
        }
    }

    /**
     * Migrates object with specific key from some version to migrator's version.
     *
     * @param key     Key of object to migrate;
     * @param version Current version of object;
     * @return New version of object after migration process;
     * @throws Migration.MigrationException Exception during object migration. Usually it indicates illegal state.
     */
    public long migrate(@NonNull final TKey key, final long version) throws Migration.MigrationException {
        if (!supportsMigrationFor(version)) {
            throw new Migration.MigrationException(String.format("Version %s of '%s' is not supported by %s", version, key, this));
        }
        return migrateInternal(key, version, oldStore, newStore);
    }

    /**
     * Internal migration logic specified by implementation.
     *
     * @param key      Key of object to migrate;
     * @param version  Current version of object;
     * @param oldStore Old store of object;
     * @param newStore new store of object;
     * @return New version of object after migration process;
     * @throws Migration.MigrationException Exception during object migration. Usually it indicates illegal state.
     */
    protected abstract long migrateInternal(@NonNull TKey key,
                                            final long version,
                                            @NonNull Store<TKey, TOldStoreObject> oldStore,
                                            @NonNull Store<TKey, TNewStoreObject> newStore) throws Migration.MigrationException;

}
