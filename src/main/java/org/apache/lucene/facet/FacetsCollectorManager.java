package org.apache.lucene.facet;

import org.apache.lucene.search.CollectorManager;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by ekeller on 05/12/2016.
 */
public class FacetsCollectorManager implements CollectorManager<FacetsCollector, FacetsCollector> {

	private FacetsCollector result;

	@Override
	public FacetsCollector newCollector() throws IOException {
		return new FacetsCollector();
	}

	@Override
	public FacetsCollector reduce(Collection<FacetsCollector> collectors) throws IOException {
		return null;
	}

	public FacetsCollector getResult() {
		return result;
	}
}
