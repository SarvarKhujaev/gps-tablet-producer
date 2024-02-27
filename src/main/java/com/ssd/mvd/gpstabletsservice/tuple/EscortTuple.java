package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public final class EscortTuple {
    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public UUID getUuidOfPolygon() {
        return this.uuidOfPolygon;
    }

    public void setUuidOfPolygon ( final UUID uuidOfPolygon ) {
        this.uuidOfPolygon = uuidOfPolygon;
    }

    public String getCountries() {
        return this.countries;
    }

    public void setCountries ( final String countries ) {
        this.countries = countries;
    }

    public List< UUID > getPatrulList() {
        return this.patrulList;
    }

    public void setPatrulList ( final List< UUID > patrulList ) {
        this.patrulList = patrulList;
    }

    public List< UUID > getTupleOfCarsList() {
        return this.tupleOfCarsList;
    }

    public void setTupleOfCarsList ( final List< UUID > tupleOfCarsList ) {
        this.tupleOfCarsList = tupleOfCarsList;
    }

    private UUID uuid;
    private UUID uuidOfPolygon; // id of polygon
    private String countries; // название страны к которой прикреплена машина

    private List< UUID > patrulList;
    private List< UUID > tupleOfCarsList;

    public UUID getUuid () {
        return this.uuid;
    }

    public EscortTuple ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "id" ) );
            this.setCountries( row.getString( "countries" ) );
            this.setUuidOfPolygon( row.getUUID( "uuidOfPolygon" ) );
            this.setPatrulList( row.getList( "patrulList", UUID.class ) );
            this.setTupleOfCarsList( row.getList( "tupleOfCarsList", UUID.class ) );
        } );
    }

    public void removePatrulAndCar ( final TupleOfCar tupleOfCar ) {
        this.getPatrulList().remove( tupleOfCar.getUuidOfPatrul() );
        this.getTupleOfCarsList().remove( tupleOfCar.getUuid() );
    }
}
