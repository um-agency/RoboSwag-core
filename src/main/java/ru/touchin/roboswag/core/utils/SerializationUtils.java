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

    public static void writeNullableObject(@NonNull final ObjectOutputStream out, @Nullable final Serializable obj) throws IOException {
        final boolean isNull = obj == null;
        out.writeBoolean(!isNull);
        if (!isNull) {
            out.writeObject(obj);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Serializable> T readNullableObject(@NonNull final ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (!in.readBoolean()) {
            return null;
        }
        return (T) in.readObject();
    }

    private SerializationUtils() {
    }

}
