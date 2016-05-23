package ru.touchin.roboswag.core.observables.collections;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public class ObservableList<TItem> extends ObservableCollection<TItem> {

    @NonNull
    private final List<TItem> items;

    public ObservableList() {
        items = new ArrayList<>();
    }

    public ObservableList(@NonNull final Collection<TItem> initialItems) {
        items = new ArrayList<>(initialItems);
    }

    public void add(@NonNull final TItem item) {
        add(items.size(), item);
    }

    public void add(final int position, @NonNull final TItem item) {
        items.add(position, item);
        notifyAboutChange(new Change(Change.Type.INSERTED, position, 1));
    }

    public void addAll(@NonNull final Collection<TItem> itemsToAdd) {
        addAll(items.size(), itemsToAdd);
    }

    public void addAll(final int position, @NonNull final Collection<TItem> itemsToAdd) {
        items.addAll(position, itemsToAdd);
        notifyAboutChange(new Change(Change.Type.INSERTED, position, itemsToAdd.size()));
    }

    public void remove(final int position) {
        items.remove(position);
        notifyAboutChange(new Change(Change.Type.REMOVED, position, 1));
    }

    public void clear() {
        final int oldSize = items.size();
        items.clear();
        notifyAboutChange(new Change(Change.Type.REMOVED, 0, oldSize));
    }

    @NonNull
    @Override
    public TItem get(final int position) {
        return items.get(position);
    }

    public void set(final int position, @NonNull final TItem item) {
        items.set(position, item);
        notifyAboutChange(new Change(Change.Type.CHANGED, position, 1));
    }

    public void set(@NonNull final Collection<TItem> items) {
        clear();
        addAll(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int size() {
        return items.size();
    }

    @NonNull
    @Override
    public Observable<TItem> loadItem(final int position) {
        return Observable.just(items.get(position));
    }

}
