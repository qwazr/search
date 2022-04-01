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

package com.qwazr.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.search.analysis.AnalyzerDefinition;
import com.qwazr.search.field.FieldDefinition;
import com.qwazr.search.index.IndexSettingsDefinition;
import com.qwazr.search.index.PostDefinition;
import com.qwazr.search.index.QueryDefinition;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.ObjectMappers;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.Assert;

public interface JsonHelpers {

    default <T> T serializeDeserializeAndCheckEquals(T source, Class<T> type) throws IOException {
        T target = ObjectMappers.JSON.readValue(ObjectMappers.JSON.writeValueAsString(source), type);
        Assert.assertEquals(source, target);
        Assert.assertEquals(source.hashCode(), target.hashCode());
        return target;
    }

    default Class<?> getBaseClass() {
        return getClass();
    }

    default Map<String, FieldDefinition> getFieldMap(String res) {
        try (final InputStream is = getBaseClass().getResourceAsStream(res)) {
            return FieldDefinition.newFieldMap(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default FieldDefinition getField(String res) {
        try (final InputStream is = getBaseClass().getResourceAsStream(res)) {
            return FieldDefinition.newField(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default Map<String, AnalyzerDefinition> getAnalyzerMap(String res) {
        try (final InputStream is = getBaseClass().getResourceAsStream(res)) {
            return AnalyzerDefinition.newAnalyzerMap(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default AnalyzerDefinition getAnalyzer(String res) {
        try (final InputStream is = getBaseClass().getResourceAsStream(res)) {
            return AnalyzerDefinition.newAnalyzer(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    default IndexSettingsDefinition getIndexSettings(String res) {
        try (InputStream is = getBaseClass().getResourceAsStream(res)) {
            return ObjectMappers.JSON.readValue(is, IndexSettingsDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    default JsonNode getJsonNode(String res) {
        try (InputStream is = getBaseClass().getResourceAsStream(res)) {
            return ObjectMappers.JSON.readTree(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default PostDefinition.Documents getDocs(String res) {
        try (InputStream is = getBaseClass().getResourceAsStream(res)) {
            return ObjectMappers.JSON.readValue(is, PostDefinition.Documents.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default PostDefinition.Document getDoc(String res) {
        try (InputStream is = getBaseClass().getResourceAsStream(res)) {
            return ObjectMappers.JSON.readValue(is, PostDefinition.Document.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default QueryDefinition getQuery(String res) {
        try (final InputStream is = getBaseClass().getResourceAsStream(res)) {
            return QueryDefinition.newQuery(IOUtils.toString(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
