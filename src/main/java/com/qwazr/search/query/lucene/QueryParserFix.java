package com.qwazr.search.query.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.graph.GraphTokenStreamFiniteStrings;

import java.io.IOException;
import java.util.Iterator;

public class QueryParserFix extends QueryParser {


	public QueryParserFix(String f, Analyzer a) {
		super(f, a);
	}

	/**
	 * Creates a boolean query from a graph token stream. The articulation points of the graph are visited in order and the queries
	 * created at each point are merged in the returned boolean query.
	 * Patch LUCENE-7878
	 */
	@Override
	protected Query analyzeGraphBoolean(String field, TokenStream source, BooleanClause.Occur operator) throws
			IOException {
		source.reset();
		GraphTokenStreamFiniteStrings graph = new GraphTokenStreamFiniteStrings(source);
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		int[] articulationPoints = graph.articulationPoints();
		int lastState = 0;
		for (int i = 0; i <= articulationPoints.length; i++) {
			int start = lastState;
			int end = -1;
			if (i < articulationPoints.length) {
				end = articulationPoints[i];
			}
			lastState = end;
			final Query queryPos;
			if (graph.hasSidePath(start)) {
				final Iterator<TokenStream> it = graph.getFiniteStrings(start, end);
				Iterator<Query> queries = new Iterator<Query>() {
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public Query next() {
						TokenStream ts = it.next();
						return createFieldQuery(ts, BooleanClause.Occur.MUST, field, getAutoGenerateMultiTermSynonymsPhraseQuery(), 0);
					}
				};
				queryPos = newGraphSynonymQuery(queries);
			} else {
				Term[] terms = graph.getTerms(field, start);
				assert terms.length > 0;
				if (terms.length == 1) {
					queryPos = newTermQuery(terms[0]);
				} else {
					queryPos = newSynonymQuery(terms);
				}
			}
			if (queryPos != null) {
				builder.add(queryPos, operator);
			}
		}
		return builder.build();
	}
}
