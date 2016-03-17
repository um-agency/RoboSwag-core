package roboswag.org.storable.exceptions;

import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 05/10/2015.
 * TODO: fill description
 */
public class MigrationException extends Exception {

    public MigrationException(@NonNull String message) {
        super(message);
    }

    public MigrationException(@NonNull String message, @NonNull Throwable throwable) {
        super(message, throwable);
    }

}
