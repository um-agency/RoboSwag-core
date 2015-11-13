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
import java.util.Locale;

public final class LcHelper {

    private static int logLevel;
    private static boolean crashOnFatalExceptions = true;
    private static LogProcessor logProcessor;

    private static final ThreadLocalValue<SimpleDateFormat> DATE_TIME_FORMATTER
            = new ThreadLocalValue<>(() -> new SimpleDateFormat("HH:mm:ss.SSS", new Locale("ru")));

    /* Returns if library should crash on fatal exceptions (default - true, set false for production) */
    public static boolean isCrashOnFatalExceptions() {
        return crashOnFatalExceptions;
    }

    /* Sets if library should crash on fatal exceptions (default - true, set false for production) */
    public static void setCrashOnFatalExceptions(final boolean crashOnFatalExceptions) {
        LcHelper.crashOnFatalExceptions = crashOnFatalExceptions;
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

    private static void logMessage(final int priority, @NonNull final String message, @Nullable final Throwable ex,
                                   final int stackTraceAdditionalDepth, Object... args) {
        if (logProcessor == null) {
            throw new IllegalStateException("Please initialize logging by calling Lc.initialize(...) method");
        }

        if (priority >= logLevel) {
            final StackTraceElement trace = Thread.currentThread().getStackTrace()[5 + stackTraceAdditionalDepth];
            final String tag = trace.getFileName() + ":" + trace.getLineNumber();

            final String formattedMessage;
            try {
                formattedMessage = String.format(message, args);
            } catch (Throwable exception) {
                Lc.fatalException(exception);
                return;
            }

            final String messageExtended = String.format("%s %s %s",
                    DATE_TIME_FORMATTER.get().format(System.currentTimeMillis()),
                    Thread.currentThread().getName(), formattedMessage);

            if (ex == null) {
                logProcessor.processLogMessage(priority, tag, messageExtended);
            } else {
                logProcessor.processLogMessage(priority, tag, messageExtended, ex);
            }
        }
    }

    public static void logMessage(final int priority, final String message, final Throwable ex, Object... args) {
        logMessage(priority, message, ex, 0, args);
    }

    /* Prints stack trace in log with DEBUG level */
    public static void printStackTrace(final String tag) {
        if (logLevel <= Log.DEBUG) {
            Log.d(tag, TextUtils.join("\n", Thread.currentThread().getStackTrace()));
        }
    }

    /* Error level log with exception */
    public static void fatalException(@NonNull final Throwable ex) {
        if (crashOnFatalExceptions) {
            if (ex instanceof ShouldNotHappenException) {
                throw (ShouldNotHappenException) ex;
            } else {
                throw new ShouldNotHappenException(ex);
            }
        } else {
            logMessage(Log.ASSERT, "Fatal exception", ex);
        }
    }

    @NonNull
    private static String logLevelToString(int logLevel) {
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