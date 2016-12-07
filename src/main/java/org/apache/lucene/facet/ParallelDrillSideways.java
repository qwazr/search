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

		return new DrillSideways.DrillSidewaysResult(buildFacetsResult(null, null,
				null));
	}
}
