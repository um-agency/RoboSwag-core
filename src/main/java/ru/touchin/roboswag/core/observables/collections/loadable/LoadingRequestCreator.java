package ru.touchin.roboswag.core.observables.collections.loadable;

import android.support.annotation.NonNull;

import rx.Observable;

public interface LoadingRequestCreator<TItem, TItemId> {

    @NonNull
    Observable<LoadedItems<TItem>> loadByItemId(@NonNull final LoadingFromRequest<TItemId> loadingFromRequest);

}