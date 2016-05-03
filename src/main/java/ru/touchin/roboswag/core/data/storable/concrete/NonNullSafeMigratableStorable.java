package ru.touchin.roboswag.core.data.storable.concrete;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.core.data.storable.Migration;
import ru.touchin.roboswag.core.data.storable.SafeConverter;
import ru.touchin.roboswag.core.data.storable.SafeStore;
import ru.touchin.roboswag.core.data.storable.Storable;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 03/05/16.
 * TODO: description
 */
public class NonNullSafeMigratableStorable<TKey, TObject, TStoreObject>
        extends NonNullSafeStorable<TKey, TObject, TStoreObject> {

    protected NonNullSafeMigratableStorable(@NonNull final TKey key,
                                            @NonNull final Class<TObject> objectClass,
                                            @NonNull final Class<TStoreObject> storeObjectClass,
                                            @NonNull final SafeStore<TKey, TStoreObject> store,
                                            @NonNull final SafeConverter<TObject, TStoreObject> converter,
                                            final boolean cloneOnGet,
                                            @NonNull final Migration<TKey> migration,
                                            @NonNull final TObject defaultValue) {
        super(key, objectClass, storeObjectClass, store, converter, cloneOnGet, migration, defaultValue);
    }

    public static class Builder<TKey, TObject, TStoreObject> extends Storable.Builder<TKey, TObject, TStoreObject> {

        public Builder(@NonNull final NonNullMigratableStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        public Builder(@NonNull final NonNullSafeStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        public Builder(@NonNull final SafeMigratableStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        @NonNull
        @Override
        public TObject getDefaultValue() {
            final TObject defaultValue = super.getDefaultValue();
            if (defaultValue == null) {
                throw new ShouldNotHappenException();
            }
            return defaultValue;
        }

        @NonNull
        @Override
        public Migration<TKey> getMigration() {
            final Migration<TKey> result = super.getMigration();
            if (result == null) {
                throw new ShouldNotHappenException();
            }
            return result;
        }

        @NonNull
        @Override
        public NonNullSafeMigratableStorable<TKey, TObject, TStoreObject> build() {
            if (storeObjectClass == null || !(store instanceof SafeStore) || !(converter instanceof SafeConverter)) {
                throw new ShouldNotHappenException();
            }
            return new NonNullSafeMigratableStorable<>(key, objectClass, storeObjectClass,
                    (SafeStore<TKey, TStoreObject>) store, (SafeConverter<TObject, TStoreObject>) converter,
                    cloneOnGet, getMigration(), getDefaultValue());
        }

    }

}
