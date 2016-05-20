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

package ru.touchin.roboswag.core.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 24/03/16.
 */
public class Changeable<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient BehaviorSubject<T> subject;

    protected Changeable() {
        //for serializer
    }

    public Changeable(@Nullable final T defaultValue) {
        subject = BehaviorSubject.create(defaultValue);
    }

    public void set(@Nullable final T value) {
        subject.onNext(value);
    }

    @Nullable
    public T get() {
        return subject.getValue();
    }

    @NonNull
    public Observable<T> observe() {
        return subject.distinctUntilChanged();
    }

    private void writeObject(final ObjectOutputStream outputStream) throws IOException {
        SerializationUtils.writeNullableObject(outputStream, subject.getValue());
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        subject = BehaviorSubject.create(SerializationUtils.<T>readNullableObject(inputStream));
    }

}