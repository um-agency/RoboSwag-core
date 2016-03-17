package roboswag.org.core;

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

import roboswag.org.core.exceptions.ObjectIsMutableException;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO: fill description
 */
public final class ObjectUtils {

    private static final List<Class> IMMUTABLE_COLLECTIONS_TYPES
            = Arrays.asList(AbstractList.class, AbstractMap.class, AbstractSet.class);

    public static boolean equals(@Nullable Object object1, @Nullable Object object2) {
        // copy of Arrays.deepEqualsElements

        if (object1 == object2) {
            return true;
        }

        if (object1 == null || object2 == null) {
            return false;
        }

        Class<?> cl1 = object1.getClass().getComponentType();
        Class<?> cl2 = object2.getClass().getComponentType();

        if (cl1 != cl2) {
            return false;
        }

        if (cl1 == null) {
            return object1.equals(object2);
        }

        /*
         * compare as arrays
         */
        if (object1 instanceof Object[]) {
            return Arrays.deepEquals((Object[]) object1, (Object[]) object2);
        } else if (cl1 == int.class) {
            return Arrays.equals((int[]) object1, (int[]) object2);
        } else if (cl1 == char.class) {
            return Arrays.equals((char[]) object1, (char[]) object2);
        } else if (cl1 == boolean.class) {
            return Arrays.equals((boolean[]) object1, (boolean[]) object2);
        } else if (cl1 == byte.class) {
            return Arrays.equals((byte[]) object1, (byte[]) object2);
        } else if (cl1 == long.class) {
            return Arrays.equals((long[]) object1, (long[]) object2);
        } else if (cl1 == float.class) {
            return Arrays.equals((float[]) object1, (float[]) object2);
        } else if (cl1 == double.class) {
            return Arrays.equals((double[]) object1, (double[]) object2);
        } else {
            return Arrays.equals((short[]) object1, (short[]) object2);
        }
    }

    public static void checkIfIsImmutable(@NonNull Class<?> objectClass) throws ObjectIsMutableException {
        checkIfIsImmutable(objectClass, false, new HashSet<>());
    }

    private static void checkIfIsImmutable(@NonNull Class<?> objectClass,
                                           boolean isSuperclass,
                                           @NonNull Set<Class> checkedClasses)
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

        for (Field field : objectClass.getDeclaredFields()) {
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

    private static boolean isImmutableCollection(@NonNull Class<?> objectClass,
                                                 @Nullable Type genericType,
                                                 @NonNull Set<Class> checkedClasses)
            throws ObjectIsMutableException {
        for (Class<?> collectionClass : IMMUTABLE_COLLECTIONS_TYPES) {
            if (collectionClass != objectClass && collectionClass != objectClass.getSuperclass()) {
                continue;
            }

            if (!(genericType instanceof ParameterizedType)) {
                throw new ObjectIsMutableException(objectClass + " is immutable collection but generic type " + genericType + " is not ParameterizedType");
            }
            for (Type parameterType : ((ParameterizedType) genericType).getActualTypeArguments()) {
                if (!(parameterType instanceof Class)) {
                    throw new ObjectIsMutableException(objectClass + " is immutable collection but generic parameterType " + parameterType + "is not ParameterizedType");
                }
                checkIfIsImmutable((Class) parameterType, false, checkedClasses);
            }
            return true;
        }
        return false;
    }

    private ObjectUtils() {
    }

}
