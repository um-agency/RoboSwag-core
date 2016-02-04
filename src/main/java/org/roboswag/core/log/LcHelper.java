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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.roboswag.core.utils.ShouldNotHappenException;
import org.roboswag.core.utils.ThreadLocalValue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public final class LcHelper {

    private static int logLevel;
    private static boolean crashOnAssertions = true;
    private static LogProcessor logProcessor;
    private static final int STACK_TRACE_CODE_SHIFT;

    private static final ThreadLocalValue<SimpleDateFormat> DATE_TIME_FORMATTER
            = new ThreadLocalValue<>(() -> new SimpleDateFormat("HH:mm:ss.SSS", new Locale("ru")));

    static {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int stackDepth;
        for (stackDepth = 0; stackDepth < stackTrace.length; stackDepth++) {
            if (stackTrace[stackDepth].getClassName().equals(LcHelper.class.getName())) {
                break;
            }
        }
        STACK_TRACE_CODE_SHIFT = stackDepth + 1;
    }

    /* Returns if library should crash on fatal exceptions (default - true, set false for production) */
    public static boolean isCrashOnAssertions() {
        return crashOnAssertions;
    }

    /* Sets if library should crash on fatal exceptions (default - true, set false for production) */
    public static void setCrashOnAssertions(final boolean crashOnAssertions) {
        LcHelper.crashOnAssertions = crashOnAssertions;
    }

    /* Returns logging level */
    public static int getLogLevel() {
        return logLevel;
    }

    /* Logging initialization */
    public static void initialize(final int logLevel) {
        initialize(logLevel, new ConsoleLogProcessor());
    }

    /* Logging initialization with different log level for Robospice and custom log processor */
    public static void initialize(final int logLevel, @NonNull final LogProcessor logProcessor) {
        LcHelper.logLevel = logLevel;
        LcHelper.logProcessor = logProcessor;
        Lc.d("Configuring Logging, minimum log level is %s", logLevelToString(logLevel));
    }

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private static void logMessage(final int priority, @NonNull final String message, @Nullable final Throwable ex,
                                   final int stackTraceAdditionalDepth, final Object... args) {
        if (logProcessor == null) {
            throw new IllegalStateException("Please initialize logging by calling Lc.initialize(...) method");
        }

        if (priority >= logLevel) {

            final String formattedMessage;
            try {
                formattedMessage = String.format(message, args);
            } catch (final Throwable exception) {
                Lc.assertion(exception);
                return;
            }

            final String messageExtended = String.format("%s %s %s",
                    DATE_TIME_FORMATTER.get().format(System.currentTimeMillis()),
                    Thread.currentThread().getName(), formattedMessage);

            final StackTraceElement trace = Thread.currentThread().getStackTrace()[STACK_TRACE_CODE_SHIFT + 2 + stackTraceAdditionalDepth];
            final String tag = trace.getFileName() + ':' + trace.getLineNumber();
            if (ex == null) {
                logProcessor.processLogMessage(priority, tag, messageExtended);
            } else {
                logProcessor.processLogMessage(priority, tag, messageExtended, ex);
            }
        }
    }

    public static void logMessage(final int priority, final String message, final Throwable ex, final Object... args) {
        logMessage(priority, message, ex, 0, args);
    }

    @NonNull
    public static String getCodePoint(@Nullable final Object caller) {
        final StackTraceElement trace = Thread.currentThread().getStackTrace()[STACK_TRACE_CODE_SHIFT];
        return (caller != null ? caller.getClass().getName() + '(' + caller.hashCode() + ") at " : "")
                + trace.getFileName() + ':' + trace.getMethodName() + ':' + trace.getLineNumber();
    }

    /* Prints stack trace in log with DEBUG level */
    public static void printStackTrace(final String tag) {
        if (logLevel <= Log.DEBUG) {
            final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            Log.d(tag, TextUtils.join("\n", Arrays.copyOfRange(stackTrace, STACK_TRACE_CODE_SHIFT, stackTrace.length)));
        }
    }

    /* Error level log with exception */
    public static void assertion(@NonNull final Throwable ex) {
        logMessage(Log.ASSERT, "Fatal exception", ex);
        if (crashOnAssertions) {
            if (ex instanceof ShouldNotHappenException) {
                throw (ShouldNotHappenException) ex;
            } else {
                throw new ShouldNotHappenException(ex);
            }
        }
    }

    @NonNull
    private static String logLevelToString(final int logLevel) {
        switch (logLevel) {
            case Log.VERBOSE:
                return "VERBOSE";
            case Log.DEBUG:
                return "DEBUG";
            case Log.INFO:
                return "INFO";
            case Log.WARN:
                return "WARN";
            case Log.ERROR:
                return "ERROR";
            case Log.ASSERT:
                return "ASSERT";
            default:
                return "UNKNOWN: " + logLevel;
        }
    }

    private LcHelper() {
    }

}