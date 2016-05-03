package ru.touchin.roboswag.core.data.storable.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.storable.Migration;
import ru.touchin.roboswag.core.data.storable.SafeConverter;
import ru.touchin.roboswag.core.data.storable.SafeStore;
import ru.touchin.roboswag.core.data.storable.Storable;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 03/05/16.
 * TODO: description
 */
public class NonNullSafeStorable<TKey, TObject, TStoreObject>
        extends NonNullStorable<TKey, TObject, TStoreObject> {

    protected NonNullSafeStorable(@NonNull final TKey key,
                                  @NonNull final Class<TObject> objectClass,
                                  @NonNull final Class<TStoreObject> storeObjectClass,
                                  @NonNull final SafeStore<TKey, TStoreObject> store,
                                  @NonNull final SafeConverter<TObject, TStoreObject> converter,
                                  final boolean cloneOnGet,
                                  @Nullable final Migration<TKey> migration,
                                  @NonNull final TObject defaultValue) {
        super(key, objectClass, storeObjectClass, store, converter, cloneOnGet, migration, defaultValue);
    }

    @NonNull
    @Override
    public TObject get() {
        try {
            return super.get();
        } catch (final Exception exception) {
            Lc.assertion(exception);
            return getDefaultValue();
        }
    }

    @Override
    public void set(@Nullable final TObject value) {
        try {
            super.set(value);
        } catch (final Exception exception) {
            Lc.assertion(exception);
        }
    }

    public static class Builder<TKey, TObject, TStoreObject> extends Storable.Builder<TKey, TObject, TStoreObject> {

        public Builder(@NonNull final NonNullStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
            super(sourceBuilder);
        }

        public Builder(@NonNull final SafeStorable.Builder<TKey, TObject, TStoreObject> sourceBuilder) {
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
        public NonNullSafeMigratableStorable.Builder<TKey, TObject, TStoreObject> setMigration(@NonNull final Migration<TKey> migration) {
            this.migration = migration;
            return new NonNullSafeMigratableStorable.Builder<>(this);
        }

        @NonNull
        @Override
        public NonNullSafeStorable<TKey, TObject, TStoreObject> build() {
            if (storeObjectClass == null || !(store instanceof SafeStore) || !(converter instanceof SafeConverter)) {
                throw new ShouldNotHappenException();
            }
            return new NonNullSafeStorable<>(key, objectClass, storeObjectClass,
                    (SafeStore<TKey, TStoreObject>) store, (SafeConverter<TObject, TStoreObject>) converter,
                    cloneOnGet, getMigration(), getDefaultValue());
        }

    }

}
