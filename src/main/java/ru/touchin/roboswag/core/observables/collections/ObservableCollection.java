package ru.touchin.roboswag.core.observables.collections;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.internal.util.RxRingBuffer;
import rx.subjects.PublishSubject;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * TODO: description
 */
public abstract class ObservableCollection<TItem> {

    @NonNull
    private final PublishSubject<Change> changeSubject = PublishSubject.create();

    protected void notifyAboutChange(@NonNull final Change change) {
        changeSubject.onNext(change);
    }

    @NonNull
    public Observable<Change> observeChanges() {
        return changeSubject;
    }

    public abstract int size();

    @NonNull
    public abstract TItem get(int position);

    @NonNull
    public abstract Observable<TItem> loadItem(int position);

    @NonNull
    @SuppressWarnings("unchecked")
    public Observable<List<TItem>> loadRange(final int first, final int last) {
        final List<Observable<List<TItem>>> itemsRequests = new ArrayList<>();

        int index = first;
        while (index <= last) {
            final List<Observable<TItem>> limitedPageRequests = new ArrayList<>();
            final int maxIndex = index + RxRingBuffer.SIZE - 1;
            while (index <= Math.min(last, maxIndex)) {
                limitedPageRequests.add(loadItem(index));
                index++;
            }
            itemsRequests.add(Observable.combineLatest(limitedPageRequests, args -> {
                final List<TItem> resultPart = new ArrayList<>(args.length);
                for (final Object item : args) {
                    if (item != null) {
                        resultPart.add((TItem) item);
                    }
                }
                return resultPart;
            }));
        }

        return Observable.combineLatest(itemsRequests, args -> {
            final List<TItem> result = new ArrayList<>();
            for (final Object resultPart : args) {
                result.addAll((List<TItem>) resultPart);
            }
            return result;
        });
    }

}
