/*
 *  Copyright (c) 2016 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.core.utils;

import android.support.annotation.NonNull;

import java.security.MessageDigest;

import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 29/08/2016.
 * Utility class to providing some string-related helper methods.
 */
public final class StringUtils {

    /**
     * Returns MD5 of string.
     *
     * @param string String to get MD5 from;
     * @return MD5 of string.
     */
    @NonNull
    public static String md5(@NonNull final String string) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(string.getBytes("UTF-8"));
            final byte[] messageDigestArray = digest.digest();

            final StringBuilder hexString = new StringBuilder();
            for (final byte messageDigest : messageDigestArray) {
                final String hex = Integer.toHexString(0xFF & messageDigest);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (final Exception exception) {
            throw new ShouldNotHappenException(exception);
        }
    }

    /**
     * Returns true if input text contains character satisfies condition.
     *
     * @param text      Text to check characters;
     * @param condition Condition of symbol;
     * @return True if some character satisfies condition.
     */
    public static boolean containsCharLike(@NonNull final String text, @NonNull final Func1<Character, Boolean> condition) {
        for (int i = 0; i < text.length(); i++) {
            if (condition.call(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if text contains any number.
     *
     * @param text Text to check;
     * @return True if there are any number in text.
     */
    public static boolean containsNumbers(@NonNull final String text) {
        return containsCharLike(text, Character::isDigit);
    }

    /**
     * Returns if text contains any lower-case character.
     *
     * @param text Text to check;
     * @return True if there are any lower-case character in text.
     */
    public static boolean containsLowerCase(@NonNull final String text) {
        return containsCharLike(text, Character::isLowerCase);
    }

    /**
     * Returns if text contains any upper-case character.
     *
     * @param text Text to check;
     * @return True if there are any upper-case character in text.
     */
    public static boolean containsUpperCase(@NonNull final String text) {
        return containsCharLike(text, Character::isUpperCase);
    }

    private StringUtils() {
    }

}
