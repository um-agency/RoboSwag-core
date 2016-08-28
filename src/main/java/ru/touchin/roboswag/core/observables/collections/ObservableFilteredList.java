package ru.touchin.roboswag.core.observables.collections;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.touchin.roboswag.core.utils.ShouldNotHappenException;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 02/06/2016.
 * {@link ObservableCollection} based on simple collection with filter inside.
 * Changing filter or collection will provide changes from {@link #observeChanges()}.
 *
 * @param <TItem> Type of collection's items.
 */
public class ObservableFilteredList<TItem> extends ObservableCollection<TItem> {

    @NonNull
    private static <TItem> List<TItem> filterCollection(@NonNull final Collection<TItem> sourceCollection,
                                                        @NonNull final Func1<TItem, Boolean> filter) {
        final List<TItem> result = new ArrayList<>(sourceCollection.size());
        for (final TItem item : sourceCollection) {
            if (filter.call(item)) {
                result.add(item);
            }
        }
        return result;
    }

    @Nullable
    private List<TItem> filteredList;
    @Nullable
    private Collection<TItem> sourceCollection;
    @Nullable
    private Func1<TItem, Boolean> filter;

    public ObservableFilteredList() {
        super();
        //do nothing
    }

    public ObservableFilteredList(@NonNull final Collection<TItem> sourceCollection) {
        super();
        this.sourceCollection = new ArrayList<>(sourceCollection);
        this.filteredList = new ArrayList<>(sourceCollection);
    }

    public ObservableFilteredList(@NonNull final Func1<TItem, Boolean> filter) {
        super();
        this.filter = filter;
    }

    public ObservableFilteredList(@NonNull final Collection<TItem> sourceCollection,
                                  @NonNull final Func1<TItem, Boolean> filter) {
        super();
        this.sourceCollection = new ArrayList<>(sourceCollection);
        this.filter = filter;
        filteredList = filterCollection(this.sourceCollection, this.filter);
    }

    /**
     * Sets collection of items to filter.
     *
     * @param sourceCollection Collection with items.
     */
    public void setSourceCollection(@Nullable final Collection<TItem> sourceCollection) {
        this.sourceCollection = sourceCollection != null ? new ArrayList<>(sourceCollection) : null;
        updateCollections();
    }

    /**
     * Sets filter that should return false as result of call to filter item.
     *
     * @param filter Function to filter item. True - item will stay, false - item will be filtered.
     */
    public void setFilter(@Nullable final Func1<TItem, Boolean> filter) {
        this.filter = filter;
        updateCollections();
    }

    private void updateCollections() {
        if (sourceCollection == null) {
            if (filteredList != null) {
                final Change<TItem> change = new Change<>(Change.Type.REMOVED, filteredList, 0);
                filteredList = null;
                notifyAboutChange(change);
            }
            return;
        }
        final List<TItem> oldFilteredList = filteredList;
        if (filter != null) {
            filteredList = filterCollection(sourceCollection, filter);
        } else {
            filteredList = new ArrayList<>(sourceCollection);
        }
        if (oldFilteredList != null) {
            final Collection<Change<TItem>> changes = Change.calculateCollectionChanges(oldFilteredList, filteredList, false);
            if (!changes.isEmpty()) {
                notifyAboutChanges(changes);
            }
        } else {
            notifyAboutChange(new Change<>(Change.Type.INSERTED, filteredList, 0));
        }
    }

    @Override
    public int size() {
        return filteredList != null ? filteredList.size() : 0;
    }

    @NonNull
    @Override
    public TItem get(final int position) {
        if (filteredList == null) {
            throw new ShouldNotHappenException();
        }
        return filteredList.get(position);
    }

    @NonNull
    @Override
    public Collection<TItem> getItems() {
        return filteredList != null ? Collections.unmodifiableCollection(filteredList) : Collections.emptyList();
    }

    @NonNull
    @Override
    public Observable<TItem> loadItem(final int position) {
        return filteredList != null && filteredList.size() > position
                ? Observable.just(filteredList.get(position))
                : Observable.just(null);
    }

}
