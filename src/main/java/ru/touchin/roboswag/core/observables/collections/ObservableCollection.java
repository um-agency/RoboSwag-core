/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.core.observables.collections;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private final PublishSubject<Collection<Change>> changesSubject = PublishSubject.create();

    protected void notifyAboutChange(@NonNull final Change change) {
        notifyAboutChanges(Collections.singleton(change));
    }

    protected void notifyAboutChanges(@NonNull final Collection<Change> changes) {
        changesSubject.onNext(changes);
    }

    @NonNull
    public Observable<Collection<Change>> observeChanges() {
        return changesSubject;
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
