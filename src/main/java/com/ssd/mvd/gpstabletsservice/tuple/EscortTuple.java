package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class EscortTuple {
    private UUID uuid; // own of id of each escortTuple
    private UUID uuidOfPolygon; // id of polygon
    private String countries; // название страны к которой прикреплена машина

    private List< UUID > patrulList;
    private List< UUID > tupleOfCarsList;

    public UUID getUuid () { return this.uuid != null ? uuid : ( this.uuid = UUID.randomUUID() ); }

    public EscortTuple ( final Row row ) { Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "id" ) );
            this.setCountries( row.getString( "countries" ) );
            this.setUuidOfPolygon( row.getUUID( "uuidOfPolygon" ) );
            this.setPatrulList( row.getList( "patrulList", UUID.class ) );
            this.setTupleOfCarsList( row.getList( "tupleOfCarsList", UUID.class ) ); } ); }
}
