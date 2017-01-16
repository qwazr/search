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
import org.apache.lucene.index.CheckIndex;

import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public class IndexCheckStatus {

	/**
	 * True if no problems were found with the index.
	 */
	final public boolean clean;

	/**
	 * True if we were unable to locate and load the segments_N file.
	 */
	final public boolean missingSegments;

	/**
	 * True if we were unable to open the segments_N file.
	 */
	final public boolean cantOpenSegments;

	/**
	 * True if we were unable to read the version number from segments_N file.
	 */
	final public boolean missingSegmentVersion;

	/**
	 * Name of latest segments_N file in the index.
	 */
	final public String segmentsFileName;

	/**
	 * Number of segments in the index.
	 */
	final public int numSegments;

	/**
	 * Empty unless you passed specific segments list to check as optional 3rd argument.
	 *
	 * @see CheckIndex#checkIndex(List)
	 */
	final public List<String> segmentsChecked;

	/**
	 * True if the index was created with a newer version of Lucene than the CheckIndex tool.
	 */
	final public boolean toolOutOfDate;

	/**
	 * How many documents will be lost to bad segments.
	 */
	final public int totLoseDocCount;

	/**
	 * How many bad segments were found.
	 */
	final public int numBadSegments;

	/**
	 * True if we checked only specific segments
	 * checkIndex(List)}) was called with non-null
	 * argument).
	 */
	final public boolean partial;

	/**
	 * The greatest segment name.
	 */
	final public int maxSegmentName;

	/**
	 * Whether the SegmentInfos.counter is greater than any of the segments' names.
	 */
	final public boolean validCounter;

	/**
	 * Holds the userData of the last commit in the index
	 */
	final public Map<String, String> userData;

	public IndexCheckStatus() {
		clean = false;
		missingSegments = false;
		cantOpenSegments = false;
		missingSegmentVersion = false;
		segmentsFileName = null;
		numSegments = 0;
		segmentsChecked = null;
		toolOutOfDate = false;
		totLoseDocCount = 0;
		numBadSegments = 0;
		partial = false;
		maxSegmentName = 0;
		validCounter = false;
		userData = null;
	}

	public IndexCheckStatus(final CheckIndex.Status status) {
		clean = status.clean;
		missingSegments = status.missingSegments;
		cantOpenSegments = status.cantOpenSegments;
		missingSegmentVersion = status.missingSegmentVersion;
		segmentsFileName = status.segmentsFileName;
		numSegments = status.numSegments;
		segmentsChecked = status.segmentsChecked;
		toolOutOfDate = status.toolOutOfDate;
		totLoseDocCount = status.totLoseDocCount;
		numBadSegments = status.numBadSegments;
		partial = status.partial;
		maxSegmentName = status.maxSegmentName;
		validCounter = status.validCounter;
		userData = status.userData;
	}
}