package ru.touchin.roboswag.core.data.storable.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.data.storable.Converter;
import ru.touchin.roboswag.core.data.storable.Migration;
import ru.touchin.roboswag.core.data.storable.Storable;
import ru.touchin.roboswag.core.data.storable.Store;
import ru.touchin.roboswag.core.log.Lc;

/**
 * Created by Gavriil Sitnikov on 03/05/16.
 * TODO: description
 */
public class SafeStorable<TKey, TObject, TStoreObject>
        extends Storable<TKey, TObject, TStoreObject> {

    protected SafeStorable(@NonNull final TKey key,
                           @NonNull final Class<TObject> objectClass,
                           @NonNull final Class<TStoreObject> storeObjectClass,
                           @NonNull final Store<TKey, TStoreObject> store,
                           @NonNull final Converter<TObject, TStoreObject> converter,
                           final boolean cloneOnGet,
                           @Nullable final Migration<TKey> migration,
                           @Nullable final TObject defaultValue) {
        super(key, objectClass, storeObjectClass, store, converter, cloneOnGet, migration, defaultValue);
    }

    @Nullable
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

}
