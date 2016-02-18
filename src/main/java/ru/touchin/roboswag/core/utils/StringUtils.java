package ru.touchin.roboswag.core.utils;

import android.support.annotation.NonNull;

import java.security.MessageDigest;

import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 13/11/2015.
 * TODO: fill description
 */
public final class StringUtils {

    /* Returns MD5 hash of text */
    @NonNull
    public static String md5(@NonNull final String text) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(text.getBytes("UTF-8"));
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

    private static boolean containsCharLike(@NonNull final String text, @NonNull final Func1<Character, Boolean> condition) {
        for (int i = 0; i < text.length(); i++) {
            if (condition.call(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsNumbers(@NonNull final String text) {
        return containsCharLike(text, Character::isDigit);
    }

    public static boolean containsLowerCase(@NonNull final String text) {
        return containsCharLike(text, Character::isLowerCase);
    }

    public static boolean containsUpperCase(@NonNull final String text) {
        return containsCharLike(text, Character::isUpperCase);
    }

    private StringUtils() {
    }

}
