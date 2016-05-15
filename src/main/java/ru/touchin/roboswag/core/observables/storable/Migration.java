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
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 06/10/2015.
 * Object that allows to migrate some store objects from one version to another by migrators passed into constructor.
 * Migrating objects should have same types of store key.
 *
 * @param <TKey> Type of key of store objects.
 */
public class Migration<TKey> {

    public static final long DEFAULT_VERSION = -1L;

    private final long latestVersion;
    @NonNull
    private final Store<TKey, Long> versionsStore;
    @NonNull
    private final List<Migrator<TKey, ?, ?>> migrators;

    @SafeVarargs
    public Migration(@NonNull final Store<TKey, Long> versionsStore,
                     final long latestVersion,
                     @NonNull final Migrator<TKey, ?, ?>... migrators) {
        this.versionsStore = versionsStore;
        this.latestVersion = latestVersion;
        this.migrators = Arrays.asList(migrators);
    }

    private long loadCurrentVersion(@NonNull final TKey key) throws MigrationException {
        final Long result;
        try {
            result = versionsStore.loadObject(Long.class, key);
        } catch (final Store.StoreException throwable) {
            throw new MigrationException(String.format("Can't get version of '%s' from %s", key, versionsStore), throwable);
        }
        return result != null ? result : DEFAULT_VERSION;
    }

    private void checkMigrationResult(@NonNull final TKey key, final long oldVersion, final long currentVersion, @Nullable final Migrator migrator)
            throws MigrationException {
        if (oldVersion > currentVersion) {
            throw new MigrationException(String.format("Version of '%s' downgraded from %s to %s [from %s by %s]",
                    key, oldVersion, currentVersion, versionsStore, migrator));
        }
        if (currentVersion > latestVersion) {
            throw new MigrationException(String.format("Version of '%s' is %s and higher than latest version %s [from %s by %s]",
                    key, oldVersion, currentVersion, versionsStore, migrator));
        }
        if (oldVersion == currentVersion && migrator != null) {
            throw new MigrationException(String.format("Version of '%s' is %s and stood same [from %s by %s]",
                    key, currentVersion, versionsStore, migrator));
        }
    }

    /**
     * Migrates some object by key to latest version.
     *
     * @param key Key of object to migrate;
     * @throws MigrationException Exception during object migration. Usually it indicates illegal state.
     */
    public void migrateToLatestVersion(@NonNull final TKey key) throws MigrationException {
        long currentVersion = loadCurrentVersion(key);

        while (currentVersion != latestVersion) {
            final long oldVersion = currentVersion;
            for (final Migrator<TKey, ?, ?> migrator : migrators) {
                if (migrator.supportsMigrationFor(currentVersion) && migrator.canMigrate(key, currentVersion)) {
                    currentVersion = migrator.migrate(key, currentVersion);
                    checkMigrationResult(key, oldVersion, currentVersion, migrator);
                }
                checkMigrationResult(key, oldVersion, currentVersion, null);
            }
        }

        try {
            versionsStore.storeObject(Long.class, key, latestVersion);
        } catch (final Store.StoreException throwable) {
            throw new MigrationException(String.format("Can't store version %s of '%s' into %s", key, currentVersion, versionsStore), throwable);
        }
    }

    public static class MigrationException extends Exception {

        public MigrationException(@NonNull final String message) {
            super(message);
        }

        public MigrationException(@NonNull final String message, @NonNull final Throwable throwable) {
            super(message, throwable);
        }

    }

}
