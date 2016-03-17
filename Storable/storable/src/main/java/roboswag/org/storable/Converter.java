package roboswag.org.storable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import roboswag.org.storable.exceptions.ConversionException;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO: fill description
 */
public interface Converter<TObject, TStoreObject> {

    @Nullable
    TStoreObject toStoreObject(@NonNull Class<TObject> objectClass,
                               @NonNull Class<TStoreObject> storeObjectClass,
                               @Nullable TObject object) throws ConversionException;

    @Nullable
    TObject toObject(@NonNull Class<TObject> objectClass,
                     @NonNull Class<TStoreObject> storeObjectClass,
                     @Nullable TStoreObject storeObject) throws ConversionException;

}
