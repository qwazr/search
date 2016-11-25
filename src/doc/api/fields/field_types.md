# The list of available field templates

Here is the list of supported Lucene fields in Qwazr.

### Text search fields

Field that are indexed for full-text search. They are supposed to be associated with an analyzer.

Name                              |Indexed|Tokenized|Stored|Facet|Sort |Filter
----------------------------------|:-----:|:-------:|:----:|:---:|:---:|:----:
StringField                       | Yes   | No      | No   | No  | No  | Yes
TextField                         | Yes   | Yes     | No   | No  | No  | Yes

### PointValues fields

Points represent numeric values and are indexed differently than ordinary text.
Instead of an inverted index, points are indexed with datastructures such as KD-trees.
These structures are optimized for operations such as range, distance, nearest-neighbor,
and point-in-polygon queries.

Name                              |Indexed|Tokenized|Stored|Facet|Sort |Filter
----------------------------------|:-----:|:-------:|:----:|:---:|:---:|:----:
DoublePoint                       | Yes   | No      | No   | No  | No  | Yes
FloatPoint                        | Yes   | No      | No   | No  | No  | Yes
IntPoint                          | Yes   | No      | No   | No  | No  | Yes
LongPoint                         | Yes   | No      | No   | No  | No  | Yes

### DocValues fields

Field that stores a per-document value for scoring, sorting or value retrieval.

Name                              |Indexed|Tokenized|Stored|Facet|Sort |Filter
----------------------------------|:-----:|:-------:|:----:|:---:|:---:|:----:
BinaryDocValuesField              | No    | No      | Yes  | No  | Yes | No
LongDocValuesField                | No    | No      | Yes  | No  | Yes | No
IntDocValuesField                 | No    | No      | Yes  | No  | Yes | No
FloatDocValuesField               | No    | No      | Yes  | No  | Yes | No
DoubleDocValuesField              | No    | No      | Yes  | No  | Yes | No

### Sorted DocValues fields

Per-Document values in a SortedDocValues are deduplicated, dereferenced, and sorted into a dictionary of unique values.
A pointer to the dictionary value (ordinal) can be retrieved for each document.
Ordinals are dense and in increasing sorted order.

Name                              |Indexed|Tokenized|Stored|Facet|Sort |Filter
----------------------------------|:-----:|:-------:|:----:|:---:|:---:|:----:
SortedDocValuesField              | No    | No      | Yes  | No  | Yes | No
SortedLongDocValuesField          | No    | No      | Yes  | No  | Yes | No
SortedIntDocValuesField           | No    | No      | Yes  | No  | Yes | No
SortedDoubleDocValuesField        | No    | No      | Yes  | No  | Yes | No
SortedFloatDocValuesField         | No    | No      | Yes  | No  | Yes | No
SortedSetDocValuesField           | No    | No      | Yes  | No  | Yes | No

### Facet fields 

Field used for facet building based on SortedSetDocValues.

Name                              |Indexed|Tokenized|Stored|Facet|Sort |Filter
----------------------------------|:-----:|:-------:|:----:|:---:|:---:|:----:
FacetField                        | Yes   | No      | No   | Yes | No  | Yes
MultiFacetField                   | Yes   | No      | No   | Yes | No  | Yes
SortedSetDocValuesFacetField      | Yes   | No      | No   | Yes | No  | Yes
SortedSetMultiDocValuesFacetField | Yes   | No      | No   | Yes | No  | Yes

### Stored fields

A field whose value is just stored.

Name                              |Indexed|Tokenized|Stored|Facet|Sort |Filter
----------------------------------|:-----:|:-------:|:----:|:---:|:---:|:----:
StoredField                       | No    | No      | Yes  | No  | No  | No


### Legacy numeric fields

Field that indexes double values for efficient range filtering and sorting.
**They are deprecated now**.
One should use PointValues for filtering and DocValues for sorting.

Name                              |Indexed|Tokenized|Stored|Facet|Sort |Filter
----------------------------------|:-----:|:-------:|:----:|:---:|:---:|:----:
DoubleField (legacy)              | Yes   | No      | No   | No  | Yes | Yes
FloatField (legacy)               | Yes   | No      | No   | No  | Yes | Yes
IntField (legacy)                 | Yes   | No      | No   | No  | Yes | Yes
LongField (legacy)                | Yes   | No      | No   | No  | Yes | Yes