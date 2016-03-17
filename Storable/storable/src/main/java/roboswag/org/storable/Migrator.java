package roboswag.org.storable;

import android.support.annotation.NonNull;

import roboswag.org.storable.exceptions.MigrationException;

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
