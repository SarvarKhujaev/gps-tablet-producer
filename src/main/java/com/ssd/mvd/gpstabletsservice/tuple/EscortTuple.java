package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.constants.Countries;
import com.datastax.driver.core.Row;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class EscortTuple {
    private UUID uuid; // own of id of each escortTuple
    private UUID uuidOfPolygon; // id of polygon
    private Countries countries;

    private List< UUID > patrulList;
    private List< UUID > tupleOfCarsList;

    public EscortTuple ( Row row ) {
        this.setUuid( row.getUUID( "id" ) );
        this.setUuidOfPolygon( row.getUUID( "polygonForEscort" ) );
        this.setTupleOfCarsList( row.getList( "carList", UUID.class ) );
        this.setTupleOfCarsList( row.getList( "patrulList", UUID.class ) );
        this.setCountries( Countries.valueOf( row.getString( "country" ) ) ); }
}
