package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.constants.Countries;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.datastax.driver.core.Row;
import java.util.List;
import lombok.Data;

@Data
public class EscortTuple {
    private Countries countries;
    private PolygonForEscort polygon;
    private List< Patrul > patrulList;
    private List< TupleOfCar > tupleOfCars;

    public EscortTuple ( Row row ) {
        this.setTupleOfCars( row.getList( "carList", TupleOfCar.class ) );
        this.setCountries( Countries.valueOf( row.getString( "country" ) ) );
        this.setPolygon( row.get( "polygonForEscort", PolygonForEscort.class ) ); }
}
