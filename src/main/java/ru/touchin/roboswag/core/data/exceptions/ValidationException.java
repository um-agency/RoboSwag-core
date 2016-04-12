package ru.touchin.roboswag.core.data.exceptions;

import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 12/04/16.
 */
public class ValidationException extends Exception {

    public ValidationException(@NonNull final String reason) {
        super(reason);
    }

}
