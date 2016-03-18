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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO: fill description
 */
public final class ObjectUtils {

    private static final List<Class> IMMUTABLE_COLLECTIONS_TYPES
            = Arrays.asList(AbstractList.class, AbstractMap.class, AbstractSet.class);

    // copy of Arrays.deepEqualsElements
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity", "PMD.CompareObjectsWithEquals"})
    public static boolean equals(@Nullable final Object object1, @Nullable final Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }

        final Class<?> elementType1 = object1.getClass().getComponentType();
        final Class<?> elementType2 = object2.getClass().getComponentType();

        if (!(elementType1 == null ? elementType2 == null : elementType1.equals(elementType2))) {
            return false;
        }
        if (elementType1 == null) {
            return object1.equals(object2);
        }
        return isArraysEquals(object1, object2, elementType1);
    }

    @SuppressWarnings("PMD.AvoidUsingShortType")
    private static boolean isArraysEquals(@NonNull final Object object1, @Nullable final Object object2, final Class<?> elementType) {
        if (object1 instanceof Object[]) {
            return Arrays.deepEquals((Object[]) object1, (Object[]) object2);
        } else if (elementType == int.class) {
            return Arrays.equals((int[]) object1, (int[]) object2);
        } else if (elementType == char.class) {
            return Arrays.equals((char[]) object1, (char[]) object2);
        } else if (elementType == boolean.class) {
            return Arrays.equals((boolean[]) object1, (boolean[]) object2);
        } else if (elementType == byte.class) {
            return Arrays.equals((byte[]) object1, (byte[]) object2);
        } else if (elementType == long.class) {
            return Arrays.equals((long[]) object1, (long[]) object2);
        } else if (elementType == float.class) {
            return Arrays.equals((float[]) object1, (float[]) object2);
        } else if (elementType == double.class) {
            return Arrays.equals((double[]) object1, (double[]) object2);
        } else {
            return Arrays.equals((short[]) object1, (short[]) object2);
        }
    }

    public static void checkIfIsImmutable(@NonNull final Class<?> objectClass) throws ObjectIsMutableException {
        checkIfIsImmutable(objectClass, false, new HashSet<>());
    }

    @SuppressWarnings({"PMD.NPathComplexity", "PMD.StdCyclomaticComplexity",
            "PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity"})
    private static void checkIfIsImmutable(@NonNull final Class<?> objectClass,
                                           final boolean isSuperclass,
                                           @NonNull final Set<Class> checkedClasses)
            throws ObjectIsMutableException {

        if (checkedClasses.contains(objectClass)) {
            return;
        }
        checkedClasses.add(objectClass);

        if (objectClass.isArray()) {
            throw new ObjectIsMutableException(objectClass + " is array which is mutable");
        }
        if (objectClass.isPrimitive() || objectClass.getSuperclass() == Number.class
                || objectClass.isEnum() || objectClass == Boolean.class
                || objectClass == String.class || objectClass == Object.class) {
            return;
        }
        if (isImmutableCollection(objectClass, objectClass.getGenericSuperclass(), checkedClasses)) {
            return;
        }

        if (!isSuperclass
                && (!Modifier.isFinal(objectClass.getModifiers())
                || (objectClass.isMemberClass() && !Modifier.isStatic(objectClass.getModifiers())))) {
            throw new ObjectIsMutableException(objectClass + " is not final and static");
        }

        for (final Field field : objectClass.getDeclaredFields()) {
            if (!Modifier.isFinal(field.getModifiers())) {
                throw new ObjectIsMutableException("Field " + field.getName() + " of class " + objectClass + " is not final");
            }
            if (isImmutableCollection(field.getType(), field.getGenericType(), checkedClasses)) {
                continue;
            }
            checkIfIsImmutable(field.getType(), false, checkedClasses);
        }

        if (objectClass.getSuperclass() != null) {
            checkIfIsImmutable(objectClass.getSuperclass(), true, checkedClasses);
        }
    }

    private static boolean isImmutableCollection(@NonNull final Class<?> objectClass,
                                                 @Nullable final Type genericType,
                                                 @NonNull final Set<Class> checkedClasses)
            throws ObjectIsMutableException {
        for (final Class<?> collectionClass : IMMUTABLE_COLLECTIONS_TYPES) {
            if (collectionClass.equals(objectClass) || collectionClass.equals(objectClass.getSuperclass())) {
                if (!(genericType instanceof ParameterizedType)) {
                    throw new ObjectIsMutableException(objectClass + " is immutable collection but generic type "
                            + genericType + " is not ParameterizedType");
                }
                for (final Type parameterType : ((ParameterizedType) genericType).getActualTypeArguments()) {
                    if (!(parameterType instanceof Class)) {
                        throw new ObjectIsMutableException(objectClass + " is immutable collection but generic parameterType "
                                + parameterType + "is not ParameterizedType");
                    }
                    checkIfIsImmutable((Class) parameterType, false, checkedClasses);
                }
                return true;
            }
        }
        return false;
    }

    private ObjectUtils() {
    }

}
