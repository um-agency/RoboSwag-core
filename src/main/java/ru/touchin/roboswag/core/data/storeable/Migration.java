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

package ru.touchin.roboswag.core.data.storeable;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.touchin.roboswag.core.data.exceptions.MigrationException;
import ru.touchin.roboswag.core.data.exceptions.StoreException;

/**
 * Created by Gavriil Sitnikov on 06/10/2015.
 * TODO: fill description
 */
public class Migration<TKey> {

    private final long latestVersion;
    @NonNull
    private final Store<TKey, Long> versionsStore;
    @NonNull
    private final List<Migrator<TKey, ?, ?>> migrators = new ArrayList<>();

    public Migration(@NonNull final Store<TKey, Long> versionsStore, final long latestVersion) {
        this.versionsStore = versionsStore;
        this.latestVersion = latestVersion;
    }

    public void addMigrator(@NonNull final Migrator<TKey, ?, ?> migrator) {
        migrators.add(migrator);
    }

    @SuppressWarnings({"PMD.NPathComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity"})
    public void migrateToLatestVersion(@NonNull final TKey key) throws MigrationException {
        Long version;
        try {
            version = versionsStore.loadObject(Long.class, key);
        } catch (StoreException throwable) {
            throw new MigrationException("Version for key " + key + " is null", throwable);
        }

        if (version == null) {
            version = Migrator.DEFAULT_VERSION;
        }

        while (!version.equals(latestVersion)) {
            final long oldVersion = version;
            boolean migrationTriggered = false;
            for (final Migrator<TKey, ?, ?> migrator : migrators) {
                if (migrator.supportMigrationFor(version) && migrator.getOldStore().contains(key)) {
                    version = migrator.migrate(key, version);
                    migrationTriggered = true;
                }
            }
            if (oldVersion > version) {
                throw new MigrationException("Version downgraded from [" + oldVersion + "] to [" + version + "]");
            } else if (oldVersion == version) {
                if (migrationTriggered) {
                    throw new MigrationException("Migration not changed version [" + version + "]");
                } else {
                    break;
                }
            } else if (version > latestVersion) {
                throw new MigrationException("Version [" + version + "] is higher than latest version [" + latestVersion + "]");
            }
        }

        try {
            versionsStore.storeObject(Long.class, key, latestVersion);
        } catch (StoreException throwable) {
            throw new MigrationException("Storing version failed for " + key, throwable);
        }
    }

}
