package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import java.util.ArrayList;
import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TupleTotalData { // contains the total data about Escort
    private PolygonForEscort polygonForEscort;
    private List< TupleOfCar > tupleOfCarList = new ArrayList<>();
    private List< Patrul > patrulList = new ArrayList<>();
}
