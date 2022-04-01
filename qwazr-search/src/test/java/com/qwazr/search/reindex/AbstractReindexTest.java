/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.reindex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexServiceInterface;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.ReindexDefinition;
import com.qwazr.search.test.units.AbstractIndexTest;
import com.qwazr.utils.ObjectMappers;
import com.qwazr.utils.WaitFor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.ws.rs.WebApplicationException;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractReindexTest extends AbstractIndexTest {

    private final static String INDEX = "reindexIndex";

    static IndexServiceInterface service;

    protected static void setup(final Supplier<Map<String, Object>> docSupplier) throws JsonProcessingException {
        service = initIndexManager(true).getService();
        service.createUpdateIndex(INDEX,
            IndexSettingsDefinition.of()
                .recordField(FieldDefinition.RECORD_FIELD)
                .primaryKey("id")
                .build()
        );
        for (int i = 0; i < 20; i++) {
            service.postJson(INDEX, false, ObjectMappers.JSON.readTree(
                ObjectMappers.JSON.writeValueAsString(
                    getRandomDocs(50, docSupplier))));
        }
        assertThat(service.getIndex(INDEX).numDocs, equalTo(1000L));
    }

    public static List<Map<String, Object>> getRandomDocs(int count,
                                                          final Supplier<Map<String, Object>> docSupplier) {
        final List<Map<String, Object>> docs = new ArrayList<>(count);
        for (int i = 0; i < count; i++)
            docs.add(docSupplier.get());
        return docs;
    }

    @Test
    public void test00NoReindexStatus() {
        final ReindexDefinition reindexStatus = service.getReindexStatus(INDEX);
        assertThat(reindexStatus, notNullValue());
        assertThat(reindexStatus.error, nullValue());
        assertThat(reindexStatus.start, nullValue());
        assertThat(reindexStatus.status, nullValue());
        assertThat(reindexStatus.end, nullValue());
        assertThat(reindexStatus.completion, nullValue());
    }

    @Test
    public void test05StopReindexStatusOnNotRunning() {
        Assert.assertThrows(
            "There is no reindexing process currently running",
            WebApplicationException.class,
            () -> service.stopReindex(INDEX));
    }

    @Test
    public void test10StartReindexStatusOnNotRunning() {
        final Date start = new Date();
        final ReindexDefinition reindexStatus = service.startReindex(INDEX, 20);
        assertThat(reindexStatus, notNullValue());
        assertThat(reindexStatus.error, nullValue());
        assertThat(reindexStatus.start, notNullValue());
        assertThat(reindexStatus.start, greaterThanOrEqualTo(start));
        assertThat(reindexStatus.status, anyOf(
            equalTo(ReindexDefinition.Status.initialized),
            equalTo(ReindexDefinition.Status.running),
            equalTo(ReindexDefinition.Status.done)));
    }

    @Test
    public void test15WaitUntilCompletion() throws InterruptedException {
        final Date date = new Date();
        WaitFor.of()
            .pauseTime(TimeUnit.SECONDS, 1)
            .timeOut(TimeUnit.MINUTES, 1)
            .until(() -> {
                final ReindexDefinition reindexStatus = service.getReindexStatus(INDEX);
                if (reindexStatus.status == ReindexDefinition.Status.done)
                    return true;
                assertThat(reindexStatus.error, nullValue());
                return false;
            });

        final ReindexDefinition reindexStatus = service.getReindexStatus(INDEX);
        assertThat(reindexStatus, notNullValue());
        assertThat(reindexStatus.error, nullValue());
        assertThat(reindexStatus.start, notNullValue());
        assertThat(reindexStatus.end, greaterThanOrEqualTo(date));
        assertThat(reindexStatus.completion, equalTo(100F));

        assertThat(service.getIndex(INDEX).numDocs, equalTo(1000L));
    }
}
