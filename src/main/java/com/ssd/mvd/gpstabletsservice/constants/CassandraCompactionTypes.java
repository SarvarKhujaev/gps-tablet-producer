package com.ssd.mvd.gpstabletsservice.constants;

/*
saves different types of compaction for Cassandra tables
 */
public class CassandraCompactionTypes {
    /*
    https://docs.datastax.com/en/cql-oss/3.3/cql/cql_reference/cqlCreateTable.html#compactSubprop__compactionSubpropertiesLCS
     */
    public static final String LEVELED_COMPACTION = "LeveledCompactionStrategy";

    /*
    https://docs.datastax.com/en/cql-oss/3.3/cql/cql_reference/cqlCreateTable.html#compactSubprop__compactionSubproperties
     */
    public static final String DATE_TIERED_COMPACTION = "DateTieredCompactionStrategy";

    /*
    https://docs.datastax.com/en/cql-oss/3.3/cql/cql_reference/cqlCreateTable.html#compactSubprop__compactionSubpropertiesSTCS
     */
    public static final String SIZE_TIERED_COMPACTION = "SizeTieredCompactionStrategy";

    /*
    https://docs.datastax.com/en/cql-oss/3.3/cql/cql_reference/cqlCreateTable.html#compactSubprop__compactionSubpropertiesTWCS
     */
    public static final String TIME_WINDOW_COMPACTION = "TimeWindowCompactionStrategy";

    /*
    Use COMPACT STORAGE to store data in the legacy (Thrift) storage engine format to conserve disk space.
    Use compact storage for the category table.

    Example:
        CREATE TABLE cycling.cyclist_category (
           category text,
           points int,
           id UUID,
           lastname text,
           PRIMARY KEY (category, points))
        WITH CLUSTERING ORDER BY (points DESC)
           AND COMPACT STORAGE;

     https://docs.datastax.com/en/cql-oss/3.3/cql/cql_reference/cqlCreateTable.html#compactSubprop__compactionSubpropertiesSTCS
     */
    public static final String COMPACT_STORAGE = "COMPACT STORAGE";
}
