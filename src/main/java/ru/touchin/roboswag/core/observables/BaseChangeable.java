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

package ru.touchin.roboswag.core.observables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import ru.touchin.roboswag.core.utils.ObjectUtils;
import ru.touchin.roboswag.core.utils.Optional;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 24/03/2016.
 * Wrapper over {@link BehaviorSubject} which could be serialized.
 * Such object is useful as view model and also as value in Android that could be passed into {@link android.os.Bundle}.
 *
 * @param <TValue>       Type of Changeable value;
 * @param <TReturnValue> Type of actual value operating by Changeable. Could be same as {@link TValue}.
 */
public abstract class BaseChangeable<TValue, TReturnValue> implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient BehaviorSubject<Optional<TValue>> valueSubject;

    public BaseChangeable(@Nullable final TValue defaultValue) {
        valueSubject = BehaviorSubject.create(new Optional<>(defaultValue));
    }

    @NonNull
    protected Observable<Optional<TValue>> observeOptionalValue() {
        return valueSubject.distinctUntilChanged();
    }

    /**
     * Sets current value.
     *
     * @param value Value to set.
     */
    public void set(@Nullable final TValue value) {
        valueSubject.onNext(new Optional<>(value));
    }

    /**
     * Returns current value.
     *
     * @return Current value.
     */
    @Nullable
    public TValue get() {
        return valueSubject.getValue().get();
    }

    /**
     * Returns {@link Observable} which is emits current value and then emitting changes of current value.
     *
     * @return Current value {@link Observable}.
     */
    @NonNull
    public abstract Observable<TReturnValue> observe();

    private void writeObject(@NonNull final ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(valueSubject.getValue());
    }

    @SuppressWarnings("unchecked")
    private void readObject(@NonNull final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        valueSubject = BehaviorSubject.create((Optional<TValue>) inputStream.readObject());
    }

    @Override
    public boolean equals(@Nullable final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final BaseChangeable<?, ?> that = (BaseChangeable<?, ?>) object;
        return ObjectUtils.equals(valueSubject.getValue(), that.valueSubject.getValue());
    }

    @Override
    public int hashCode() {
        return valueSubject.getValue() != null ? valueSubject.getValue().hashCode() : 0;
    }

}
