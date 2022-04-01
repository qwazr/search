/*
 * Copyright 2017-2020 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils.concurrent;

import java.io.Closeable;
import java.util.concurrent.Semaphore;

public interface AutoLockSemaphore {

    default Lock acquire() throws AcquireException {
        return Lock.EMPTY;
    }

    AutoLockSemaphore UNLIMITED = new AutoLockSemaphore() {
    };

    Reject REJECTED = new Reject();

    static AutoLockSemaphore of(int permits) {
        return permits == 0 ? REJECTED : permits < 0 ? UNLIMITED : new Impl(permits);
    }

    class Impl implements AutoLockSemaphore {

        private final Semaphore semaphore;

        private Impl(int permits) {
            semaphore = new Semaphore(permits);
        }

        @Override
        public Lock acquire() throws AcquireException {
            try {
                return new SemaphoreLock(semaphore);
            } catch (InterruptedException e) {
                throw new AcquireException(e);
            }
        }
    }

    class Reject implements AutoLockSemaphore {

        public Lock acquire() throws AcquireException {
            throw new AcquireException("Permission rejected");
        }
    }

    interface Lock extends Closeable {

        default void close() {
        }

        Lock EMPTY = new Lock() {
        };
    }

    final class SemaphoreLock implements Lock {

        private final Semaphore semaphore;

        SemaphoreLock(final Semaphore semaphore) throws InterruptedException {
            this.semaphore = semaphore;
            semaphore.acquire();
        }

        @Override
        public void close() {
            semaphore.release();
        }

    }

    class AcquireException extends RuntimeException {

        AcquireException(String message) {
            super(message);
        }

        AcquireException(Exception cause) {
            super(cause);
        }
    }
}
