package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.inspectors.CollectionsInspector;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import java.util.List;

// contains the total data about Escort
public class TupleTotalData extends CollectionsInspector {
    public List< TupleOfCar > getTupleOfCarList() {
        return this.tupleOfCarList;
    }

    public List< Patrul > getPatrulList() {
        return this.patrulList;
    }

    public void setPolygonForEscort( final PolygonForEscort polygonForEscort ) {
        this.polygonForEscort = polygonForEscort;
    }

    private PolygonForEscort polygonForEscort;
    private List< TupleOfCar > tupleOfCarList = super.newList();
    private List< Patrul > patrulList = super.newList();
}
