package ru.touchin.roboswag.core.observables.collections;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import ru.touchin.roboswag.core.observables.collections.changes.DefaultCollectionsChangesCalculator;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Gavriil Sitnikov on 02/06/2016.
 * {@link ObservableCollection} based on simple collection with filter inside.
 * Changing filter or collection will provide changes from {@link #observeChanges()}.
 *
 * @param <TItem> Type of collection's items.
 */
public class ObservableFilteredList<TItem> extends ObservableCollection<TItem> {

    // we need to filter on 1 thread to prevent parallel filtering
    private static final Scheduler FILTER_SCHEDULER = Schedulers.from(Executors.newSingleThreadExecutor());

    @NonNull
    private static <TItem> List<TItem> filterCollection(@NonNull final Collection<TItem> sourceCollection,
                                                        @Nullable final Func1<TItem, Boolean> filter) {
        if (filter == null) {
            return new ArrayList<>(sourceCollection);
        }
        final List<TItem> result = new ArrayList<>(sourceCollection.size());
        for (final TItem item : sourceCollection) {
            if (filter.call(item)) {
                result.add(item);
            }
        }
        return result;
    }

    @NonNull
    private List<TItem> filteredList;
    @NonNull
    private ObservableCollection<TItem> sourceCollection;
    @Nullable
    private Func1<TItem, Boolean> filter;
    @Nullable
    private Subscription sourceCollectionSubscription;

    public ObservableFilteredList() {
        this(new ArrayList<>(), null);
    }

    public ObservableFilteredList(@NonNull final Func1<TItem, Boolean> filter) {
        this(new ArrayList<>(), filter);
    }

    public ObservableFilteredList(@NonNull final Collection<TItem> sourceCollection, @Nullable final Func1<TItem, Boolean> filter) {
        this(new ObservableList<>(sourceCollection), filter);
    }

    public ObservableFilteredList(@NonNull final ObservableCollection<TItem> sourceCollection, @Nullable final Func1<TItem, Boolean> filter) {
        super();
        this.filter = filter;
        this.sourceCollection = sourceCollection;
        this.filteredList = filterCollection(this.sourceCollection.getItems(), this.filter);
        updateInternal();
    }

    /**
     * Sets collection of items to filter.
     *
     * @param sourceCollection Collection with items.
     */
    public void setSourceCollection(@Nullable final ObservableCollection<TItem> sourceCollection) {
        this.sourceCollection = sourceCollection != null ? sourceCollection : new ObservableList<>();
        updateInternal();
    }

    /**
     * Sets collection of items to filter.
     *
     * @param sourceCollection Collection with items.
     */
    public void setSourceCollection(@Nullable final Collection<TItem> sourceCollection) {
        this.sourceCollection = sourceCollection != null ? new ObservableList<>(sourceCollection) : new ObservableList<>();
        updateInternal();
    }

    /**
     * Sets filter that should return false as result of call to filter item.
     *
     * @param filter Function to filter item. True - item will stay, false - item will be filtered.
     */
    public void setFilter(@Nullable final Func1<TItem, Boolean> filter) {
        this.filter = filter;
        updateInternal();
    }

    private void updateInternal() {
        if (sourceCollectionSubscription != null) {
            sourceCollectionSubscription.unsubscribe();
            sourceCollectionSubscription = null;
        }
        sourceCollectionSubscription = sourceCollection.observeItems()
                .observeOn(FILTER_SCHEDULER)
                .subscribe(items -> {
                    final List<TItem> oldFilteredList = filteredList;
                    filteredList = filterCollection(items, filter);
                    final DefaultCollectionsChangesCalculator<TItem> calculator
                            = new DefaultCollectionsChangesCalculator<>(oldFilteredList, filteredList, false);
                    notifyAboutChanges(calculator.calculateInsertedItems(), calculator.calculateRemovedItems(), calculator.calculateChanges());
                });
    }

    /**
     * Updates collection by current filter. Use it if some item's parameter which is important for filtering have changing.
     */
    public void update() {
        updateInternal();
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
    public Collection<TItem> getItems() {
        return Collections.unmodifiableCollection(filteredList);
    }

    /**
     * Returns source non-filtered observable collection of items.
     *
     * @return Non-filtered collection of items.
     */
    @NonNull
    public ObservableCollection<TItem> getSourceCollection() {
        return sourceCollection;
    }

}
