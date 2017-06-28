package ru.touchin.roboswag.core.observables.collections.changes;

import android.support.annotation.NonNull;

import java.util.Collection;

public interface CollectionsChangesCalculator {

    @NonNull
    Collection<Change> calculateChanges();

}
