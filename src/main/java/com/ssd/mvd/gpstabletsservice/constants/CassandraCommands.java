package com.ssd.mvd.gpstabletsservice.constants;

/*
хранит все команды связанные с БД
*/
public final class CassandraCommands {
    public static String SELECT_ALL = "SELECT * FROM";
    public static String INSERT_INTO = "INSERT INTO";
    /*
    https://foundev.medium.com/domain-modeling-around-deletes-1cc9b6da0d24 <- link for docs
    */
    public static String DELETE = "DELETE FROM";
    public static String UPDATE = "UPDATE";

    public static String CREATE_KEYSPACE = "CREATE KEYSPACE";
    public static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    public static String CREATE_INDEX = "CREATE INDEX IF NOT EXISTS ON";
    public static String CREATE_TYPE = "CREATE TYPE IF NOT EXISTS";


    public static String IF_EXISTS = "IF EXISTS;";
    public static String IF_NOT_EXISTS = "IF NOT EXISTS;";

    public static String BEGIN_BATCH = "BEGIN BATCH ";
    public static String APPLY_BATCH = "APPLY BATCH;";

    /*
    When upserting data if any columns are missing from the JSON, the value in the missing column is overwritten with null (by default)

    EXAMPLE:
        INSERT INTO cycling.cyclist_category JSON '{
              "category" : "GC",
              "points" : 780,
              "id" : "829aa84a-4bba-411f-a4fb-38167a987cda",
              "lastname" : "SUTHERLAND"
              }';

    https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/useInsertJSON.html
     */
    public static final String JSON = "JSON";

    /*
    Use the DEFAULT UNSET option to only overwrite values found in the JSON string:
     */
    public static final String DEFAULT_UNSET = "DEFAULT UNSET";

    /*
    To set the TTL for data, use the USING TTL keywords. The TTL function may be used to retrieve the TTL information.

    The USING TTL keywords can be used to insert data into a table for a specific duration of time.
    To determine the current time-to-live for a record, use the TTL function.

    https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/useTTL.html <- link for docs
     */
    public static final String USING_TTL = "USING TTL %d";

    /*
    Create a Change Data Capture (CDC) log on the table.

    cdc = TRUE | FALSE
     */
    public static final String CDC = "cdc = %s";

    public static final String WITH_COMMENT = "comment = '%s'";
    public static final String WITH_COMPACTION = """
            compaction = { 'class' : '%s' }
            """;

    public static final String WITH_CLUSTERING_ORDER = "WITH CLUSTERING ORDER BY ( %s )";

    public static final String WITH_COMPRESSION = """
            COMPRESSION = {
                'class' : '%s',
                'chunk_length_in_kb' : %d
            }
            """;

    public static final String WITH_CACHING = """
            caching = {
                 'keys' : '%s',
                 'rows_per_partition' : '%s'
            }
            """;

    /*
    TTL FUNCTIONS

    https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/useTTL.html <- link for docs
    */
    public static final String WITH_TTL = "default_time_to_live = %d";

    public static final String MEM_TABLE_FLUSH_PERIOD = "memtable_flush_period_in_ms = %d";

    public static final String BLOOM_FILTER = "bloom_filter_fp_chance = %f";

    /*
    The minimum number of seconds after an SSTable is created
    before Cassandra considers the SSTable for tombstone compaction.
    Cassandra begins tombstone compaction SSTable's tombstone_threshold exceeds value of the following property.
    DEFAULT;
        86400 (one day)
     */
    public static final String TOMBSTONE_INTERVAL = "tombstone_compaction_interval = %f";

    /*
    CREATE TABLE cycling.calendar (
      race_id int,
      race_start_date timestamp,
      race_end_date timestamp,
      race_name text,
      PRIMARY KEY (race_id, race_start_date, race_end_date)
    ) WITH CLUSTERING ORDER BY (race_start_date ASC, race_end_date ASC)
      AND bloom_filter_fp_chance = 0.01
      AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
      AND comment = ''
      AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy',
        'max_threshold': '32', 'min_threshold': '4'}
      AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
      AND crc_check_chance = 1.0
      AND default_time_to_live = 0
      AND gc_grace_seconds = 864000
      AND max_index_interval = 2048
      AND memtable_flush_period_in_ms = 0
      AND min_index_interval = 128
      AND nodesync = {'enabled' : 'true'}
      AND speculative_retry = '99PERCENTILE';
     */
}
