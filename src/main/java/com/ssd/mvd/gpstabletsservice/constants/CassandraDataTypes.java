package com.ssd.mvd.gpstabletsservice.constants;

public enum CassandraDataTypes {
    ALL,

    NONE,

    BLOB,

    /*
    UTF8 encoded string
    */
    TEXT,

    /*
    An IP address, either IPv4 (4 bytes long) or IPv6 (16 bytes long).
    Note that there is no inet constant, IP address should be input as strings.
     */
    INET,

    /*
    A blob (Binary Large OBject) data type represents a constant hexadecimal number defined as 0[xX](hex)+
    where hex is a hexadecimal character, such as [0-9a-fA-F]. For example, 0xcafe.
    The maximum theoretical size for a blob is 2 GB. The practical limit on blob size, however, is less than 1 MB.
    A blob type is suitable for storing a small image or short string.
     */
    VARCHAR,

    INT,
    /*
    64-bit signed long
    */
    BIGINT,
    /*
    64-bit IEEE-754 floating point
    */
    DOUBLE,
    /*
    8-bit signed int
    */
    TINYINT,

    UUID,
    /*
    Either true or false
    */
    BOOLEAN,
    /*
    A timestamp (date and time) with millisecond precision.
    */
    TIMESTAMP,

    /*
    Collections are meant for storing/denormalizing relatively small amount of data.
    They work well for things like “the phone numbers of a given user”, “labels applied to an email”, etc.
    But when items are expected to grow unbounded (“all messages sent by a user”, “events registered by a sensor”),
    then collections are not appropriate and a specific table (with clustering columns) should be used.
    Concretely, (non-frozen) collections have the following noteworthy characteristics and limitations:

        Individual collections are not indexed internally.
        Which means that even to access a single element of a collection,
        the while collection has to be read (and reading one is not paged internally).

        While insertion operations on sets and maps never incur a read-before-write internally, some operations on lists do.
        Further, some lists operations are not idempotent by nature (see the section on lists below for details),
        making their retry in case of timeout problematic. It is thus advised to prefer sets to lists when possible.

    Please note that while some of those limitations may or may not be removed/improved upon in the future,
    it is an antipattern to use a (single) collection to store large amounts of data
    */

    /*
    A set is a (sorted) collection of unique values.

    Examples:
        INSERT INTO images (name, owner, tags)
        VALUES ('cat.jpg', 'jsmith', { 'pet', 'cute' });

        // Replace the existing set entirely
        UPDATE images SET tags = { 'kitten', 'cat', 'lol' } WHERE name = 'cat.jpg';

        Adding one or multiple elements (as this is a set, inserting an already existing element is a no-op):
            UPDATE images SET tags = tags + { 'gray', 'cuddly' } WHERE name = 'cat.jpg';

        Removing one or multiple elements (if an element doesn’t exist, removing it is a no-op but no error is thrown):
            UPDATE images SET tags = tags - { 'cat' } WHERE name = 'cat.jpg';
     */
    SET,

    /*
    A map is a (sorted) set of key-value pairs, where keys are unique and the map is sorted by its keys.

    Updating or inserting one or more elements:
        UPDATE users SET favs['author'] = 'Ed Poe' WHERE id = 'jsmith';
        UPDATE users SET favs = favs + { 'movie' : 'Cassablanca', 'band' : 'ZZ Top' } WHERE id = 'jsmith';

    Removing one or more element (if an element doesn’t exist, removing it is a no-op but no error is thrown):
        DELETE favs['author'] FROM users WHERE id = 'jsmith';
        UPDATE users SET favs = favs - { 'movie', 'band'} WHERE id = 'jsmith';

    Note that for removing multiple elements in a map, you remove from it a set of keys.
     */
    MAP,

    /*
    The append and prepend operations are not idempotent by nature. So in particular,
    if one of these operation timeout, then retrying the operation is not safe,
    and it may (or may not) lead to appending/prepending the value twice.
     */
    LIST,

    TUPLE,

    FROZEN,

    /*
    A duration with nanosecond precision
    Values of the duration type are encoded as 3 signed integer of variable lengths.
    The first integer represents the number of months, the second the number of days and the third the number of nanoseconds.
    This is due to the fact that the number of days in a month can change,
    and a day can have 23 or 25 hours depending on the daylight saving.
    Internally, the number of months and days are decoded as 32 bits integers whereas the number of nanoseconds is decoded as a 64 bits integer.

    Example:
        INSERT INTO RiderResults (rider, race, result)
        VALUES ('Christopher Froome', 'Tour de France', 89h4m48s);

    Duration columns cannot be used in a table’s PRIMARY KEY.
    This limitation is due to the fact that durations cannot be ordered.
    It is effectively not possible to know if 1mo is greater than 29d without a date context.

    A 1d duration is not equal to a 24h one as the duration type has been created to be able to support daylight saving.
    */
    DURATION,

    /*
    Version 1 UUID, generally used as a “conflict-free”
    Values of the timestamp type are encoded as 64-bit signed integers representing a number of milliseconds
    since the standard base time known as the epoch: January 1, 1970, at 00:00:00 GMT.

    Timestamps can be input in CQL either using their value as an integer,
    or using a string that represents an ISO 8601 date.
    For instance, all the values below are valid timestamp values for Mar 2, 2011, at 04:05:00 AM, GMT:

        1299038700000

        '2011-02-03 04:05+0000'

        '2011-02-03 04:05:00+0000'

        '2011-02-03 04:05:00.000+0000'

        '2011-02-03T04:05+0000'

        '2011-02-03T04:05:00+0000'

        '2011-02-03T04:05:00.000+0000'
    */
    TIMEUUID,

    /*
    Counter column (64-bit signed value).

    The counter type is used to define counter columns.
    A counter column is a column whose value is a 64-bit signed integer and on which 2 operations are supported:
        incrementing and decrementing (see the UPDATE statement for syntax). Note that the value of a counter cannot be set:
        a counter does not exist until first incremented/decremented, and that first increment/decrement is made as if the prior value was 0.

    Counters have a number of important limitations:
        They cannot be used for columns part of the PRIMARY KEY of a table.

        A table that contains a counter can only contain counters.
        In other words, either all the columns of a table outside the PRIMARY KEY have the counter type, or none of them have it.

        Counters do not support expiration.

        The deletion of counters is supported, but is only guaranteed to work the first time you delete a counter.
        In other words, you should not re-update a counter that you have deleted (if you do, proper behavior is not guaranteed).

        Counter updates are, by nature, not idemptotent.
        An important consequence is that if a counter update fails unexpectedly (timeout or loss of connection to the coordinator node),
        the client has no way to know if the update has been applied or not. In particular, replaying the update may or may not lead to an over count.
    */
    COUNTER,
}
