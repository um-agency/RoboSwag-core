package org.roboswag.core.utils;

import android.support.annotation.NonNull;

import java.security.MessageDigest;

/**
 * Created by Gavriil Sitnikov on 13/11/2015.
 * TODO: fill description
 */
public class StringUtils {

    /* Returns MD5 hash of string */
    @NonNull
    public static String md5(@NonNull final String string) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(string.getBytes("UTF-8"));
            final byte[] messageDigestArray = digest.digest();

            final StringBuilder hexString = new StringBuilder();
            for (final byte messageDigest : messageDigestArray) {
                hexString.append(Integer.toHexString(0xFF & messageDigest));
            }
            return hexString.toString();
        } catch (final Exception e) {
            throw new ShouldNotHappenException(e);
        }
    }

}
