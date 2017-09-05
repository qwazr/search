package com.qwazr.search.test;

import com.qwazr.search.collector.BaseCollector;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;

/**
 * Created by ekeller on 02/01/2017.
 */
public class ClassicMaxCollector extends BaseCollector<Long> {

	private long max = 0;

	public ClassicMaxCollector(final String name) {
		super(name);
	}

	@Override
	public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
		return new RandomCollector(context.reader());
	}

	@Override
	public Long getResult() {
		return max;
	}

	public class RandomCollector implements LeafCollector {

		private final NumericDocValues qtyDocValues;

		RandomCollector(final LeafReader reader) throws IOException {
			qtyDocValues = reader.getNumericDocValues(AnnotatedRecord.DV_QUANTITY_FIELD);
		}

		@Override
		final public void setScorer(final Scorer scorer) throws IOException {

		}

		@Override
		final public void collect(final int doc) throws IOException {
			if (qtyDocValues == null)
				return;
			final long m = qtyDocValues.get(doc);
			if (m > max)
				max = m;
		}
	}
}
