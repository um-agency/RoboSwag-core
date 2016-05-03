package ru.touchin.roboswag.core.data.storable.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.exceptions.ConversionException;
import ru.touchin.roboswag.core.data.exceptions.MigrationException;
import ru.touchin.roboswag.core.data.exceptions.StoreException;
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
public class NonNullStorable<TKey, TObject, TStoreObject> extends Storable<TKey, TObject, TStoreObject> {

    protected NonNullStorable(@NonNull final TKey key,
                              @NonNull final Class<TObject> objectClass,
                              @NonNull final Class<TStoreObject> storeObjectClass,
                              @NonNull final Store<TKey, TStoreObject> store,
                              @NonNull final Converter<TObject, TStoreObject> converter,
                              final boolean cloneOnGet,
                              @Nullable final Migration<TKey> migration,
                              @NonNull final TObject defaultValue) {
        super(key, objectClass, storeObjectClass, store, converter, cloneOnGet, migration, defaultValue);
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
    public TObject get() throws StoreException, ConversionException, MigrationException {
        final TObject result = super.get();
        return result != null ? result : getDefaultValue();
    }

    public static class Builder<TKey, TObject, TStoreObject> extends Storable.Builder<TKey, TObject, TStoreObject> {

        public Builder(@NonNull final Storable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        @NonNull
        @Override
        public TObject getDefaultValue() {
            final TObject result = super.getDefaultValue();
            if (result == null) {
                throw new ShouldNotHappenException();
            }
            return result;
        }

        @NonNull
        @Override
        public NonNullMigratableStorable.Builder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
            this.migration = migration;
            return new NonNullMigratableStorable.Builder<>(this);
        }

        @NonNull
        @Override
        public NonNullSafeStorable.Builder<TKey, TObject, TStoreObject> setSafeStore(@NonNull final Class<TStoreObject> storeObjectClass,
                                                                                     @NonNull final SafeStore<TKey, TStoreObject> store,
                                                                                     @NonNull final SafeConverter<TObject, TStoreObject> converter) {
            this.storeObjectClass = storeObjectClass;
            this.store = store;
            this.converter = converter;
            return new NonNullSafeStorable.Builder<>(this);
        }

        @NonNull
        @Override
        public NonNullStorable<TKey, TObject, TStoreObject> build() {
            if (storeObjectClass == null || store == null || converter == null) {
                throw new ShouldNotHappenException();
            }
            return new NonNullStorable<>(key, objectClass, storeObjectClass, store, converter, cloneOnGet, getMigration(), getDefaultValue());
        }

    }

}
