package ru.touchin.roboswag.core.observables.collections.loadable;

import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public interface ItemWithId<TItemId> {

    @NonNull
    TItemId getItemId();

}
