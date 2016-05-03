package ru.touchin.roboswag.core.data.storable.concrete;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.core.data.storable.Converter;
import ru.touchin.roboswag.core.data.storable.Migration;
import ru.touchin.roboswag.core.data.storable.SafeConverter;
import ru.touchin.roboswag.core.data.storable.SafeStore;
import ru.touchin.roboswag.core.data.storable.Storable;
import ru.touchin.roboswag.core.data.storable.Store;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 03/05/16.
 * TODO: description
 */
public class NonNullMigratableStorable<TKey, TObject, TStoreObject> extends NonNullStorable<TKey, TObject, TStoreObject> {

    protected NonNullMigratableStorable(@NonNull final TKey key,
                                        @NonNull final Class<TObject> objectClass,
                                        @NonNull final Class<TStoreObject> storeObjectClass,
                                        @NonNull final Store<TKey, TStoreObject> store,
                                        @NonNull final Converter<TObject, TStoreObject> converter,
                                        final boolean cloneOnGet,
                                        @NonNull final Migration<TKey> migration,
                                        @NonNull final TObject defaultValue) {
        super(key, objectClass, storeObjectClass, store, converter, cloneOnGet, migration, defaultValue);
    }

    public static class Builder<TKey, TObject, TStoreObject> extends Storable.Builder<TKey, TObject, TStoreObject> {

        public Builder(@NonNull final NonNullStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        public Builder(@NonNull final MigratableStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
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
        public Storable.Builder<TKey, TObject, TStoreObject> setSafeStore(@NonNull final Class<TStoreObject> storeObjectClass, @NonNull final SafeStore<TKey, TStoreObject> store, @NonNull final SafeConverter<TObject, TStoreObject> converter) {
            this.storeObjectClass = storeObjectClass;
            this.store = store;
            this.converter = converter;
            return new NonNullSafeMigratableStorable.Builder<>(this);
        }

        @NonNull
        @Override
        public NonNullMigratableStorable<TKey, TObject, TStoreObject> build() {
            if (storeObjectClass == null || store == null || converter == null) {
                throw new ShouldNotHappenException();
            }
            return new NonNullMigratableStorable<>(key, objectClass, storeObjectClass, store, converter, cloneOnGet, getMigration(), getDefaultValue());
        }

    }

}
