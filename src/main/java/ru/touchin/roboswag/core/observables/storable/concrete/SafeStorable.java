package ru.touchin.roboswag.core.observables.storable.concrete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.touchin.roboswag.core.observables.storable.Storable;
import ru.touchin.roboswag.core.observables.storable.builders.SafeMigratableStorableBuilder;
import ru.touchin.roboswag.core.observables.storable.builders.SafeStorableBuilder;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * {@link Storable} that should not throw exceptions on set or get.
 * If this rules are violated then it will throw {@link ShouldNotHappenException}.
 *
 * @param <TKey>         Type of key to identify object;
 * @param <TObject>      Type of actual object;
 * @param <TStoreObject> Type of store object. Could be same as {@link TObject}.
 */
public class SafeStorable<TKey, TObject, TStoreObject> extends Storable<TKey, TObject, TStoreObject> {

    public SafeStorable(@NonNull final SafeStorableBuilder<TKey, TObject, TStoreObject> builderCore) {
        super(builderCore);
    }

    public SafeStorable(@NonNull final SafeMigratableStorableBuilder<TKey, TObject, TStoreObject> builderCore) {
        super(builderCore);
    }

    @Nullable
    @Override
    public TObject getSync() {
        try {
            return super.getSync();
        } catch (final Exception exception) {
            throw new ShouldNotHappenException(exception);
        }
    }

    @Override
    public void setSync(@Nullable final TObject newValue) {
        try {
            super.setSync(newValue);
        } catch (final Exception exception) {
            throw new ShouldNotHappenException(exception);
        }
    }

}
