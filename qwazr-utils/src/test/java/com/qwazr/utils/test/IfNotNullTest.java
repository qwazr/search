package com.qwazr.utils.test;

import com.qwazr.utils.IfNotNull;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class IfNotNullTest {

    private Integer getIncrementObject(final int counter) {
        return (counter % 2) == 1 ? 1 : null;
    }

    private final static int LOOP = 10000;

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void useOptionalIfPresent() {
        final AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < LOOP; i++)
            Optional.ofNullable(getIncrementObject(i)).ifPresent(counter::addAndGet);
        assertThat(counter.get(), equalTo(LOOP / 2));

    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void useIfNotNullConsume() {
        final AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < LOOP; i++)
            IfNotNull.apply(getIncrementObject(i), counter::addAndGet);
        assertThat(counter.get(), equalTo(LOOP / 2));
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    public void useIfNotNullConsumeEx() throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < LOOP; i++)
            IfNotNull.applyEx(getIncrementObject(i), counter::addAndGet);
        assertThat(counter.get(), equalTo(LOOP / 2));
    }
}
