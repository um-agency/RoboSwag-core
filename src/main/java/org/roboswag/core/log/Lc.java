/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
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

package org.roboswag.core.log;

import android.support.annotation.NonNull;
import android.util.Log;

import org.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 13/11/2015.
 * TODO: fill description
 */
@SuppressWarnings("PMD.ShortClassName")
public final class Lc {

    /* Debug level log */
    @SuppressWarnings({"PMD.ShortMethodName", "PMD.AvoidDuplicateLiterals", "checkstyle:methodname"})
    public static void d(final String message, final Object... args) {
        LcHelper.logMessage(Log.DEBUG, message, null, args);
    }

    /* Debug level log with exception */
    @SuppressWarnings({"PMD.ShortMethodName", "checkstyle:methodname"})
    public static void d(@NonNull final Throwable ex, final String message, final Object... args) {
        LcHelper.logMessage(Log.DEBUG, message, ex, args);
    }

    /* Info level log */
    @SuppressWarnings({"PMD.ShortMethodName", "checkstyle:methodname"})
    public static void i(final String message, final Object... args) {
        LcHelper.logMessage(Log.INFO, message, null, args);
    }

    /* Info level log with exception */
    @SuppressWarnings({"PMD.ShortMethodName", "checkstyle:methodname"})
    public static void i(@NonNull final Throwable ex, final String message, final Object... args) {
        LcHelper.logMessage(Log.INFO, message, ex);
    }

    /* Warning level log */
    @SuppressWarnings({"PMD.ShortMethodName", "checkstyle:methodname"})
    public static void w(final String message, final Object... args) {
        LcHelper.logMessage(Log.WARN, message, null, args);
    }

    /* Warning level log with exception */
    @SuppressWarnings({"PMD.ShortMethodName", "checkstyle:methodname"})
    public static void w(@NonNull final Throwable ex, final String message, final Object... args) {
        LcHelper.logMessage(Log.WARN, message, ex, args);
    }

    /* Error level log */
    @SuppressWarnings({"PMD.ShortMethodName", "checkstyle:methodname"})
    public static void e(final String message, final Object... args) {
        LcHelper.logMessage(Log.ERROR, message, null, args);
    }

    /* Error level log with exception */
    @SuppressWarnings({"PMD.ShortMethodName", "checkstyle:methodname"})
    public static void e(@NonNull final Throwable ex, final String message, final Object... args) {
        LcHelper.logMessage(Log.ERROR, message, ex, args);
    }

    /* Error level log with exception */
    public static void assertion(@NonNull final String message) {
        LcHelper.assertion(new ShouldNotHappenException(message));
    }

    /* Error level log with exception */
    public static void assertion(@NonNull final Throwable ex) {
        LcHelper.assertion(ex);
    }

    private Lc() {
    }

}
