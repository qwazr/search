/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import com.qwazr.utils.concurrent.BooleanSupplierEx;
import com.qwazr.utils.concurrent.DoubleSupplierEx;
import com.qwazr.utils.concurrent.FloatSupplierEx;
import com.qwazr.utils.concurrent.IntSupplierEx;
import com.qwazr.utils.concurrent.LongSupplierEx;
import com.qwazr.utils.concurrent.RunnableEx;
import com.qwazr.utils.concurrent.SupplierEx;

public class ExceptionUtils extends org.apache.commons.lang3.exception.ExceptionUtils {

    public static <E extends Exception> void bypass(final RunnableEx<E> runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Exception> boolean bypass(final BooleanSupplierEx<E> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Exception> int bypass(final IntSupplierEx<E> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Exception> long bypass(final LongSupplierEx<E> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Exception> float bypass(final FloatSupplierEx<E> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <E extends Exception> double bypass(final DoubleSupplierEx<E> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T, E extends Exception> T bypass(final SupplierEx<T, E> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
