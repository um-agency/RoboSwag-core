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

package ru.touchin.roboswag.core.observables.collections.changes;

import android.support.annotation.Nullable;

/**
 * Created by Gavriil Sitnikov on 23/05/16.
 * Class representing simple change of collection like insertion, remove or replacing/changing items.
 */
public abstract class Change {

    /**
     * Represents a insert operation in collection.
     */
    public static class Inserted extends Change {

        private final int position;
        private final int count;

        public Inserted(final int position, final int count) {
            super();
            this.position = position;
            this.count = count;
        }

        public int getPosition() {
            return position;
        }

        public int getCount() {
            return count;
        }

    }

    /**
     * Represents a remove operation from collection.
     */
    public static class Removed extends Change {

        private final int position;
        private final int count;

        public Removed(final int position, final int count) {
            super();
            this.position = position;
            this.count = count;
        }

        public int getPosition() {
            return position;
        }

        public int getCount() {
            return count;
        }

    }

    /**
     * Represents a move operation in collection.
     */
    public static class Moved extends Change {

        private final int fromPosition;
        private final int toPosition;

        public Moved(final int fromPosition, final int toPosition) {
            super();
            this.fromPosition = fromPosition;
            this.toPosition = toPosition;
        }

        public int getFromPosition() {
            return fromPosition;
        }

        public int getToPosition() {
            return toPosition;
        }

    }

    /**
     * Represents a modification operation in a collection.
     */
    public static class Changed extends Change {

        private final int position;
        private final int count;
        @Nullable
        private final Object payload;

        public Changed(final int position, final int count, @Nullable final Object payload) {
            super();
            this.position = position;
            this.count = count;
            this.payload = payload;
        }

        public int getPosition() {
            return position;
        }

        public int getCount() {
            return count;
        }

        @Nullable
        public Object getPayload() {
            return payload;
        }

    }

}
