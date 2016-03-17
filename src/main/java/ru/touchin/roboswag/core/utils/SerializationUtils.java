package ru.touchin.roboswag.core.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 16/03/2016.
 * TODO: fill description
 */
public final class SerializationUtils {

    public static void writeNullableObject(@NonNull final ObjectOutputStream outputStream, @Nullable final Serializable obj) throws IOException {
        final boolean isNull = obj == null;
        outputStream.writeBoolean(!isNull);
        if (!isNull) {
            outputStream.writeObject(obj);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Serializable> T readNullableObject(@NonNull final ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException {
        if (!inputStream.readBoolean()) {
            return null;
        }
        return (T) inputStream.readObject();
    }

    private SerializationUtils() {
    }

}
