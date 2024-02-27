package com.ssd.mvd.gpstabletsservice.constants;

public class CassandraFunctions {
    public static final String NOW = "now()";
    public static final String UUID = "uuid()";
    public static final String TO_TIMESTAMP = "toTimestamp( %s )";

    /*
    These functions convert a native type into binary data (blob), and convert a blob back into a native type:
    https://docs.datastax.com/en/dse/6.0/cql/cql/cql_using/refBlob.html <- link for docs
    */

    /*
    For every native non-blob data type supported by CQL,
    the typeAsBlob() function takes an argument of that data type and returns it as a blob.
     */
    public static final String TYPE_AS_BLOB = "%sAsBlob( %s )";
    /*
    Conversely, the blobAsType() function takes a 64-bit blob argument
    and converts it to a value of the specified data type, if possible.
     */
    public static final String BLOB_AS_TYPE = "blobAs%s( %s )";
}
