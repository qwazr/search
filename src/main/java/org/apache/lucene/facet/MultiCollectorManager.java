package org.apache.lucene.facet;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CollectorManager;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ekeller on 05/12/2016.
 */
public class MultiCollectorManager implements CollectorManager<MultiCollectorManager.Collectors, Object[]> {

	final private CollectorManager<Collector, ?>[] collectorManagers;

	public MultiCollectorManager(final CollectorManager... collectorManagers) {
		this.collectorManagers = collectorManagers;
	}

	@Override
	public Collectors newCollector() throws IOException {
		return new Collectors();
	}

	@Override
	public Object[] reduce(Collection<Collectors> reducableCollectors) throws IOException {
		final int size = reducableCollectors.size();
		final Object[] results = new Object[collectorManagers.length];
		for (int i = 0; i < collectorManagers.length; i++) {
			final List<Collector> reducableCollector = new ArrayList<>(size);
			for (Collectors collectors : reducableCollectors)
				reducableCollector.add(collectors.collectors[i]);
			results[i] = collectorManagers[i].reduce(reducableCollector);
		}
		return results;
	}

	public class Collectors implements Collector {

		private final Collector[] collectors;

		private Collectors() throws IOException {
			collectors = new Collector[collectorManagers.length];
			for (int i = 0; i < collectors.length; i++)
				collectors[i] = collectorManagers[i].newCollector();
		}

		@Override
		final public LeafCollector getLeafCollector(final LeafReaderContext context) throws IOException {
			return new LeafCollectors(context);
		}

		@Override
		final public boolean needsScores() {
			for (Collector collector : collectors)
				if (collector.needsScores())
					return true;
			return false;
		}

		public class LeafCollectors implements LeafCollector {

			private final LeafCollector[] leafCollectors;

			private LeafCollectors(final LeafReaderContext context) throws IOException {
				leafCollectors = new LeafCollector[collectors.length];
				for (int i = 0; i < collectors.length; i++)
					leafCollectors[i] = collectors[i].getLeafCollector(context);
			}

			@Override
			final public void setScorer(final Scorer scorer) throws IOException {
				for (LeafCollector leafCollector : leafCollectors)
					if (leafCollector != null)
						leafCollector.setScorer(scorer);
			}

			@Override
			final public void collect(final int doc) throws IOException {
				for (LeafCollector leafCollector : leafCollectors)
					if (leafCollector != null)
						leafCollector.collect(doc);
			}
		}
	}

}
