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

public class ConsoleLogProcessor implements LogProcessor {

    @Override
    public void processLogMessage(final int logLevel, final String tag, final String message) {
        switch (logLevel) {
            case Log.DEBUG:
                Log.d(tag, message);
                break;
            case Log.INFO:
                Log.i(tag, message);
                break;
            case Log.WARN:
                Log.w(tag, message);
                break;
            case Log.ERROR:
            case Log.ASSERT:
            default:
                Log.e(tag, message);
                break;
        }
    }

    @Override
    public void processLogMessage(final int logLevel, final String tag, final String message, @NonNull final Throwable ex) {
        switch (logLevel) {
            case Log.DEBUG:
                Log.d(tag, message, ex);
                break;
            case Log.INFO:
                Log.i(tag, message, ex);
                break;
            case Log.WARN:
                Log.w(tag, message, ex);
                break;
            case Log.ERROR:
            case Log.ASSERT:
                Log.e(tag, message, ex);
                break;
            default:
                throw new IllegalStateException("Unsupported log level: " + logLevel);
        }
    }
}