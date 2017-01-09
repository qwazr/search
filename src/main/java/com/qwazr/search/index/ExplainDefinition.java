/**
 * Copyright 2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.search.index;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.lucene.search.Explanation;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@JsonInclude(Include.NON_EMPTY)
public class ExplainDefinition {

	final public String description;
	final public ExplainDefinition[] details;
	final public Float value;
	final public Boolean is_match;

	public ExplainDefinition() {
		description = null;
		details = null;
		value = null;
		is_match = null;
	}

	ExplainDefinition(final Explanation explanation) {
		description = explanation.getDescription();
		value = explanation.getValue();
		is_match = explanation.isMatch();
		details = toArray(explanation.getDetails());
	}

	private ExplainDefinition[] toArray(final Explanation[] explanations) {
		if (explanations == null)
			return null;
		final ExplainDefinition[] explains = new ExplainDefinition[explanations.length];
		int i = 0;
		for (Explanation explanation : explanations)
			explains[i++] = new ExplainDefinition(explanation);
		return explains;
	}

	private final static String[] DOT_PREFIX = { "digraph G {",
			"rankdir = LR;",
			"node[shape=record];",
			"label = \"\";",
			"center = 1;",
			"ranksep = \"0.4\";",
			"nodesep = \"0.25\";" };

	private final static String[] DOT_SUFFIX = { "}" };

	private int writeDot(final int id, final PrintWriter pw) {

		final String parentNodeId = "n" + id;
		int nextNodeId = id + 1;
		pw.print(parentNodeId);
		pw.print(" [label=");
		pw.print('"');
		pw.print(value);
		pw.print('|');
		pw.print(WordUtils.wrap(description, 28).replace("\"", "\\\"").replace("\n", "\\n").replace("|", "\\|"));
		pw.println("\"]");
		if (details != null) {
			for (ExplainDefinition exp : details) {
				final int childNodeId = nextNodeId;
				nextNodeId = exp.writeDot(childNodeId, pw);
				pw.print(parentNodeId);
				pw.print(" -> n");
				pw.println(childNodeId);
			}
		}
		return nextNodeId;
	}

	static String toDot(final ExplainDefinition explain) throws IOException {
		try (final StringWriter sw = new StringWriter()) {
			try (final PrintWriter pw = new PrintWriter(sw)) {

				for (String t : DOT_PREFIX)
					pw.println(t);

				explain.writeDot(1, pw);

				for (String t : DOT_SUFFIX)
					pw.println(t);
				pw.close();
				sw.close();
				return sw.toString();
			}
		}
	}
}