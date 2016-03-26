# The list of available field templates

| Name                              |  Indexed  | Tokenized | Stored | DocValues | Facet | Sort |
| --------------------------------- | ----------| ----------| ------ | --------- | ----- | ---- |
| DoubleField                       | Yes       | No        | No     | No        | No    | Yes  |
| FloatField                        | Yes       | No        | No     | No        | No    | Yes  |
| IntField                          | Yes       | No        | No     | No        | No    | Yes  |
| LongField                         | No        | No        | No     | No        | No    | Yes  |
| LongDocValuesField                | No        | No        | Yes    | Yes       | No    | Yes  |
| IntDocValuesField                 | No        | No        | Yes    | Yes       | No    | Yes  |
| FloatDocValuesField               | No        | No        | Yes    | Yes       | No    | Yes  |
| DoubleDocValuesField              | No        | No        | Yes    | Yes       | No    | Yes  |
| SortedDocValuesField              | No        | No        | Yes    | Yes       | No    | Yes  |
| SortedLongDocValuesField          | No        | No        | Yes    | Yes       | No    | Yes  |
| SortedIntDocValuesField           | No        | No        | Yes    | Yes       | No    | Yes  |
| SortedDoubleDocValuesField        | No        | No        | Yes    | Yes       | No    | Yes  |
| SortedFloatDocValuesField         | No        | No        | Yes    | Yes       | No    | Yes  |
| SortedSetDocValuesField           | No        | No        | Yes    | Yes       | No    | Yes  |
| BinaryDocValuesField              | No        | No        | Yes    | Yes       | No    | Yes  |
| StoredField                       | No        | No        | Yes    | No        | No    | No   |
| StringField                       | Yes       | No        | No     | No        | No    | No   |
| TextField                         | Yes       | Yes       | No     | No        | No    | No   |
| FacetField                        | Yes       | No        | No     | No        | Yes   | No   |
| MultiFacetField                   | Yes       | No        | No     | No        | Yes   | No   |
| SortedSetDocValuesFacetField      | Yes       | No        | No     | Yes       | Yes   | No   |
| SortedSetMultiDocValuesFacetField | Yes       | No        | No     | Yes       | Yes   | No   |
