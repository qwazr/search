/*
 * Copyright 2016-2020 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import com.qwazr.utils.concurrent.ThreadUtils;
import org.hamcrest.MatcherAssert;
import static org.hamcrest.Matchers.lessThan;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TimeTrackerTest {

    private TimeTracker feedTimeTracker(final TimeTracker timeTracker) {
        timeTracker.next("test1");
        ThreadUtils.sleep(10, TimeUnit.MILLISECONDS);
        timeTracker.next("test2");
        return timeTracker;
    }

    @Test
    public void testContentAndEqualityWithDurations() {

        final TimeTracker timeTracker = feedTimeTracker(TimeTracker.withDurations());
        final TimeTracker.Status status1 = timeTracker.getStatus();
        final TimeTracker.Status status2 = timeTracker.getStatus();

        Assert.assertEquals(status1, status2);

        timeTracker.next("test3");
        final TimeTracker.Status status3 = timeTracker.getStatus();
        Assert.assertNotEquals(status1, status3);
        MatcherAssert.assertThat(status3.totalTime, lessThan(1000L));

        Assert.assertNotNull(status3.durations.get("test1"));
        Assert.assertNotNull(status3.durations.get("test2"));
        Assert.assertNotNull(status3.durations.get("test3"));
    }

    @Test
    public void testContentAndEqualityWithoutDurations() throws InterruptedException {

        final TimeTracker timeTracker = feedTimeTracker(TimeTracker.noDurations());
        final TimeTracker.Status status1 = timeTracker.getStatus();
        Assert.assertNull(status1.durations);

        final TimeTracker.Status status2 = timeTracker.getStatus();
        Assert.assertEquals(status1, status2);

        Thread.sleep(100);
        timeTracker.next("test3");
        final TimeTracker.Status status3 = timeTracker.getStatus();
        Assert.assertNotEquals(status1, status3);
        MatcherAssert.assertThat(status3.totalTime, lessThan(10000L));
        Assert.assertNull(status3.durations);
    }

    @Test
    public void statusJsonSerialisation() throws IOException {

        final TimeTracker timeTracker = feedTimeTracker(TimeTracker.withDurations());
        final TimeTracker.Status status1 = timeTracker.getStatus();
        final TimeTracker.Status status2 =
                ObjectMappers.JSON.readValue(ObjectMappers.JSON.writeValueAsString(status1), TimeTracker.Status.class);
        Assert.assertEquals(status1, status2);
    }

    @Test
    public void testGetter() {
        final TimeTracker.Status status = feedTimeTracker(TimeTracker.withDurations()).getStatus();
        Assert.assertEquals(status.startTime, status.getStartTime());
        Assert.assertEquals(status.totalTime, status.getTotalTime());
        Assert.assertEquals(status.unknownTime, status.getUnknownTime());
        Assert.assertEquals(status.durations, status.getDurations());
    }
}
