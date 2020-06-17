package com.qwazr.search.field;

import com.qwazr.search.analysis.SmartAnalyzerSet;
import com.qwazr.search.annotations.Index;
import com.qwazr.search.annotations.SmartField;
import com.qwazr.utils.Equalizer;
import com.qwazr.utils.HashUtils;
import com.qwazr.utils.RandomUtils;
import java.io.Serializable;
import java.util.Objects;

@Index(name = "SmartFieldSorted", schema = "TestQueries", primaryKey = "id", recordField = FieldDefinition.RECORD_FIELD)
public class SmartFieldRecord extends Equalizer.Immutable<SmartFieldRecord> implements Serializable {

    @SmartField(type = SmartFieldDefinition.Type.TEXT, index = true)
    final public String id;

    @SmartField(type = SmartFieldDefinition.Type.LONG, sort = true)
    final public Long longSort;

    @SmartField(type = SmartFieldDefinition.Type.INTEGER, facet = true)
    final public Integer intFacet;

    @SmartField(type = SmartFieldDefinition.Type.TEXT, index = true, analyzer = SmartAnalyzerSet.english)
    final public String text;

    public SmartFieldRecord() {
        this(null, null, null, null);
    }

    private SmartFieldRecord(String id, Long longSort, Integer intFacet, String text) {
        super(SmartFieldRecord.class);
        this.id = id;
        this.longSort = longSort;
        this.intFacet = intFacet;
        this.text = text;
    }

    @Override
    protected boolean isEqual(final SmartFieldRecord o) {
        return Objects.equals(id, o.id)
            && Objects.equals(longSort, o.longSort)
            && Objects.equals(intFacet, o.intFacet)
            && Objects.equals(text, o.text);
    }

    @Override
    protected int computeHashCode() {
        return Objects.hash(id, longSort, intFacet, text);
    }

    static public SmartFieldRecord random() {
        return new SmartFieldRecord(
            HashUtils.newTimeBasedUUID().toString(),
            System.currentTimeMillis(),
            RandomUtils.nextInt(0, 10),
            RandomUtils.alphanumeric(10));
    }
}
