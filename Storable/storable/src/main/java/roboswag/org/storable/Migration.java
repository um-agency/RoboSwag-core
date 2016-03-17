package roboswag.org.storable;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import roboswag.org.storable.exceptions.MigrationException;
import roboswag.org.storable.exceptions.StoreException;

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

    public Migration(@NonNull Store<TKey, Long> versionsStore, long latestVersion) {
        this.versionsStore = versionsStore;
        this.latestVersion = latestVersion;
    }

    public void addMigrator(@NonNull Migrator<TKey, ?, ?> migrator) {
        migrators.add(migrator);
    }

    public void migrateToLatestVersion(@NonNull TKey key) throws MigrationException {
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
            long oldVersion = version;
            boolean migrationTriggered = false;
            for (Migrator<TKey, ?, ?> migrator : migrators) {
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
