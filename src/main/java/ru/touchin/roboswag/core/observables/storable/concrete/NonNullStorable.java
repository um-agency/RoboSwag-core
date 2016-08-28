package ru.touchin.roboswag.core.observables.storable.concrete;

import android.support.annotation.NonNull;

import ru.touchin.roboswag.core.observables.storable.Converter;
import ru.touchin.roboswag.core.observables.storable.Migration;
import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.Store;
import ru.touchin.roboswag.core.observables.storable.builders.NonNullMigratableStorableBuilder;
import ru.touchin.roboswag.core.observables.storable.builders.NonNullStorableBuilder;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * {@link Storable} that should return not null value on get.
 * If this rule is violated then it will throw {@link ShouldNotHappenException}.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class NonNullStorable<TKey, TObject, TStoreObject> extends Storable<TKey, TObject, TStoreObject> {

    public NonNullStorable(@NonNull final NonNullStorableBuilder<TKey, TObject, TStoreObject> builderCore) {
        super(builderCore);
    }

    public NonNullStorable(@NonNull final NonNullMigratableStorableBuilder<TKey, TObject, TStoreObject> builderCore) {
        super(builderCore);
    }

    @NonNull
    @Override
    public TObject getSync() throws Store.StoreException, Converter.ConversionException, Migration.MigrationException {
        final TObject result = super.getSync();
        if (result == null) {
            throw new ShouldNotHappenException();
        }
        return result;
    }

}
