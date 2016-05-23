package ru.touchin.roboswag.core.observables.collections.loadable;

import java.util.Collection;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public interface LoadedItems<TItem> {

    boolean haveMoreItems();

    Collection<TItem> getItems();

}
