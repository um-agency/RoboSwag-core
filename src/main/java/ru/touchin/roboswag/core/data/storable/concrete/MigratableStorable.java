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
public class MigratableStorable<TKey, TObject, TStoreObject> extends Storable<TKey, TObject, TStoreObject> {

    protected MigratableStorable(@NonNull final TKey key,
                                 @NonNull final Class<TObject> objectClass,
                                 @NonNull final Class<TStoreObject> storeObjectClass,
                                 @NonNull final Store<TKey, TStoreObject> store,
                                 @NonNull final Converter<TObject, TStoreObject> converter,
                                 final boolean cloneOnGet,
                                 @NonNull final Migration<TKey> migration) {
        super(key, objectClass, storeObjectClass, store, converter, cloneOnGet, migration, null);
    }

    public static class Builder<TKey, TObject, TStoreObject> extends Storable.Builder<TKey, TObject, TStoreObject> {

        public Builder(@NonNull final Storable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
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
        public NonNullMigratableStorable.Builder<TKey, TObject, TStoreObject> setDefaultValue(@NonNull final TObject defaultValue) {
            this.defaultValue = defaultValue;
            return new NonNullMigratableStorable.Builder<>(this);
        }

        @NonNull
        @Override
        public Storable.Builder<TKey, TObject, TStoreObject> setSafeStore(@NonNull final Class<TStoreObject> storeObjectClass, @NonNull final SafeStore<TKey, TStoreObject> store, @NonNull final SafeConverter<TObject, TStoreObject> converter) {
            this.storeObjectClass = storeObjectClass;
            this.store = store;
            this.converter = converter;
            return new SafeMigratableStorable.Builder<>(this);
        }

        @NonNull
        @Override
        public MigratableStorable<TKey, TObject, TStoreObject> build() {
            if (storeObjectClass == null || store == null || converter == null) {
                throw new ShouldNotHappenException();
            }
            return new MigratableStorable<>(key, objectClass, storeObjectClass, store, converter, cloneOnGet, getMigration());
        }

    }

}
