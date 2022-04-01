/*
 * Copyright 2015-2019 Emmanuel Keller / QWAZR
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

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ExecutorUtilsTest {

    @Test
    public void closeTest() throws InterruptedException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final long startTime = System.currentTimeMillis();
        executorService.submit(() -> ThreadUtils.sleep(3, TimeUnit.SECONDS));
        ExecutorUtils.close(executorService, 10, TimeUnit.SECONDS);
        final long duration = System.currentTimeMillis() - startTime;
        assertThat(duration, greaterThanOrEqualTo(3000L));
        assertTrue(executorService.isShutdown());
        assertTrue(executorService.isTerminated());
    }
}
