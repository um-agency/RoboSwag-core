package roboswag.org.storable.exceptions;

import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO: fill description
 */
public class ConversionException extends Exception {

    public ConversionException(@NonNull String message) {
        super(message);
    }

    public ConversionException(@NonNull String message, @NonNull Throwable throwable) {
        super(message, throwable);
    }

}
