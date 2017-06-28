package ru.touchin.roboswag.core.observables.collections.changes;

import android.support.annotation.NonNull;

public interface SameItemsPredicate<TItem> {

    boolean test(@NonNull TItem item1, @NonNull TItem item2);

}
