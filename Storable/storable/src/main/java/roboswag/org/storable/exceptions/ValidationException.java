package roboswag.org.storable.exceptions;

import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO: fill description
 */
public class ValidationException extends Exception {

    public ValidationException(@NonNull String message) {
        super(message);
    }

    public ValidationException(@NonNull String message, @NonNull Throwable throwable) {
        super(message, throwable);
    }

}
