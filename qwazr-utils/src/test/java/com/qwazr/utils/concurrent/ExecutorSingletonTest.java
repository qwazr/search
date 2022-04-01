/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorSingletonTest {

    @Test(expected = NullPointerException.class)
    public void invalidConstructorParameters() {
        new ExecutorSingleton(0, null);
    }

    @Test
    public void externalExecutorService() throws InterruptedException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            final ExecutorSingleton singleton = new ExecutorSingleton(executorService);
            Assert.assertEquals(executorService, singleton.getExecutorService());
            singleton.close();
            Assert.assertFalse(executorService.isShutdown());
            Assert.assertFalse(executorService.isTerminated());
        } finally {
            ExecutorUtils.close(executorService, 1, TimeUnit.MINUTES);
        }
    }

    @Test
    public void internalExecutorService() throws InterruptedException {
        final ExecutorSingleton singleton = new ExecutorSingleton(1, TimeUnit.MINUTES);
        try {
            final ExecutorService executorService = singleton.getExecutorService();
            Assert.assertNotNull(executorService);
            singleton.close();
            Assert.assertTrue(executorService.isShutdown());
            Assert.assertTrue(executorService.isTerminated());
        } finally {
            ExecutorUtils.close(singleton.getExecutorService(), 1, TimeUnit.MINUTES);
        }
    }

}
