package ru.touchin.roboswag.core.data.storable.concrete;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.core.data.storable.Converter;
import ru.touchin.roboswag.core.data.storable.Migration;
import ru.touchin.roboswag.core.data.storable.Store;

/**
 * Created by Gavriil Sitnikov on 03/05/16.
 * TODO: description
 */
public class SafeMigratableStorable<TKey, TObject, TStoreObject>
        extends SafeStorable<TKey, TObject, TStoreObject> {

    protected SafeMigratableStorable(@NonNull final TKey key,
                                     @NonNull final Class<TObject> objectClass,
                                     @NonNull final Class<TStoreObject> storeObjectClass,
                                     @NonNull final Store<TKey, TStoreObject> store,
                                     @NonNull final Converter<TObject, TStoreObject> converter,
                                     final boolean cloneOnGet,
                                     @NonNull final Migration<TKey> migration,
                                     @NonNull final TObject defaultValue) {
        super(key, objectClass, storeObjectClass, store, converter, cloneOnGet, migration, defaultValue);
    }

}
