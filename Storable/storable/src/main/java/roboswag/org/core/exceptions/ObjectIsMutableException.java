package roboswag.org.core.exceptions;

import android.support.annotation.NonNull;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO: fill description
 */
public class ObjectIsMutableException extends Exception {

    public ObjectIsMutableException(@NonNull String message){
        super(message);
    }

}
