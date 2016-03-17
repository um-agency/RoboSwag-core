package roboswag.org.storable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import roboswag.org.storable.exceptions.StoreException;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO: fill description
 */
public interface Store<TKey, TStoreObject> {

    boolean contains(@NonNull TKey key);

    void storeObject(@NonNull Class<TStoreObject> storeObjectClass,
                     @NonNull TKey key,
                     @Nullable TStoreObject storeObject) throws StoreException;

    @Nullable
    TStoreObject loadObject(@NonNull Class<TStoreObject> storeObjectClass, @NonNull TKey key) throws StoreException;

}
