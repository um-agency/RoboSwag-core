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

package ru.touchin.roboswag.core.log;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class ConsoleLogProcessor implements LogProcessor {

    private static final int MAX_LOG_LENGTH = 4000;

    @Override
    public void processLogMessage(final int logLevel, @NonNull final String tag, @NonNull final String message) {
        logMessage(logLevel, tag, message, null);
    }

    @Override
    public void processLogMessage(final int logLevel, @NonNull final String tag, @NonNull final String message, @NonNull final Throwable throwable) {
        logMessage(logLevel, tag, message, throwable);
    }

    private void logMessage(final int logLevel, @NonNull final String tag, @NonNull final String message, @Nullable final Throwable throwable) {
        final String messageToLog = message + (throwable != null ? '\n' + Log.getStackTraceString(throwable) : "");
        for (int i = 0, length = messageToLog.length(); i < length; i++) {
            int newline = messageToLog.indexOf('\n', i);
            newline = newline != -1 ? newline : length;
            do {
                int end = Math.min(newline, i + MAX_LOG_LENGTH);
                Log.println(logLevel, tag, messageToLog.substring(i, end));
                i = end;
            } while (i < newline);
        }
    }


}