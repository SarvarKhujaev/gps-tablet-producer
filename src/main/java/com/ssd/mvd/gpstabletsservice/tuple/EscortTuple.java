package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.constants.Countries;
import com.datastax.driver.core.Row;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscortTuple {
    private UUID uuid; // own of id of each escortTuple
    private UUID uuidOfPolygon; // id of polygon
    private Countries countries;

    private List< UUID > patrulList;
    private List< UUID > tupleOfCarsList;

    public UUID getUuid () { return this.uuid != null ? uuid : ( this.uuid = UUID.randomUUID() ); }

    public EscortTuple ( Row row ) {
        this.setUuid( row.getUUID( "id" ) );
        this.setUuidOfPolygon( row.getUUID( "uuidOfPolygon" ) );
        this.setTupleOfCarsList( row.getList( "patrulList", UUID.class ) );
        this.setCountries( Countries.valueOf( row.getString( "countries" ) ) );
        this.setTupleOfCarsList( row.getList( "tupleOfCarsList", UUID.class ) ); }
}
