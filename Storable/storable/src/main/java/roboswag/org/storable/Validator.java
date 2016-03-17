package roboswag.org.storable;

import android.support.annotation.Nullable;

import roboswag.org.storable.exceptions.ValidationException;

/**
 * Created by Gavriil Sitnikov on 04/10/2015.
 * TODO: fill description
 */
public interface Validator<T> {

    void validate(@Nullable T value) throws ValidationException;

}
