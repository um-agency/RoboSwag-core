package ru.touchin.roboswag.core.observables.collections;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 02/06/2016.
 * TODO: fill description
 */
public class ObservableFilteredList<TItem> extends ObservableCollection<TItem> {

    @NonNull
    private static <TItem> Collection<TItem> filterCollection(@NonNull final Collection<TItem> sourceCollection,
                                                              @NonNull final Func1<TItem, Boolean> filter) {
        final List<TItem> result = new ArrayList<>(sourceCollection.size());
        for (final TItem item : sourceCollection) {
            if (filter.call(item)) {
                result.add(item);
            }
        }
        return result;
    }

    @NonNull
    private final ObservableList<TItem> filteredList = new ObservableList<>();
    @NonNull
    private Collection<TItem> sourceCollection = new ArrayList<>();
    @Nullable
    private Func1<TItem, Boolean> filter;

    public ObservableFilteredList() {
        filteredList.observeChanges().subscribe(change -> {
            //do not change - bug of RetroLambda
            notifyAboutChanges(change);
        });
    }

    public void setSourceCollection(@NonNull final Collection<TItem> sourceCollection) {
        this.sourceCollection = sourceCollection;
        if (filter != null) {
            filteredList.set(filterCollection(sourceCollection, filter));
        } else {
            filteredList.set(sourceCollection);
        }
    }

    public void setFilter(@NonNull final Func1<TItem, Boolean> filter) {
        this.filter = filter;
        filteredList.set(filterCollection(sourceCollection, filter));
    }

    @Override
    public int size() {
        return filteredList.size();
    }

    @NonNull
    @Override
    public TItem get(final int position) {
        return filteredList.get(position);
    }

    @NonNull
    @Override
    public Observable<TItem> loadItem(final int position) {
        return filteredList.loadItem(position);
    }

}
