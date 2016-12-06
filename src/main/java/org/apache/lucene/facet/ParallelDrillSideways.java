package org.apache.lucene.facet;

import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ekeller on 05/12/2016.
 */
public class ParallelDrillSideways extends DrillSideways {

	public ParallelDrillSideways(IndexSearcher searcher, FacetsConfig config, SortedSetDocValuesReaderState state) {
		super(searcher, config, state);
	}

	public DrillSideways.DrillSidewaysResult search(DrillDownQuery query, CollectorManager hitCollectorManager)
			throws IOException {

		final Map<String, Integer> drillDownDims = query.getDims();

		final FacetsCollectorManager drillDownCollectorManager = new FacetsCollectorManager();

		if (drillDownDims.isEmpty()) {
			// There are no drill-down dims, so there is no
			// drill-sideways to compute:
			searcher.search(query, new MultiCollectorManager(hitCollectorManager, drillDownCollectorManager));
			return new DrillSideways.DrillSidewaysResult(
					buildFacetsResult(drillDownCollectorManager.getResult(), null, null), null);
		}

		Query baseQuery = query.getBaseQuery();
		if (baseQuery == null) {
			// TODO: we could optimize this pure-browse case by
			// making a custom scorer instead:
			baseQuery = new MatchAllDocsQuery();
		}
		Query[] drillDownQueries = query.getDrillDownQueries();

		FacetsCollector[] drillSidewaysCollectors = new FacetsCollector[drillDownDims.size()];
		for (int i = 0; i < drillSidewaysCollectors.length; i++) {
			drillSidewaysCollectors[i] = new FacetsCollector();
		}

		final FacetsCollector drillDownCollector = new FacetsCollector();
		final DrillSidewaysQuery dsq =
				new DrillSidewaysQuery(baseQuery, drillDownCollector, drillSidewaysCollectors, drillDownQueries,
						scoreSubDocsAtOnce());
		//TODO EK for manager
		/*if (hitCollector.needsScores() == false) {
			// this is a horrible hack in order to make sure IndexSearcher will not
			// attempt to cache the DrillSidewaysQuery
			hitCollector = new FilterCollector(hitCollector) {
				@Override
				public boolean needsScores() {
					return true;
				}
			};
		}*/
		searcher.search(dsq, hitCollectorManager);

		return new DrillSideways.DrillSidewaysResult(buildFacetsResult(drillDownCollector, drillSidewaysCollectors,
				drillDownDims.keySet().toArray(new String[drillDownDims.size()])), null);
	}
}
