package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TupleTotalData { // contains the total data about Escort
    private PolygonForEscort polygonForEscort;
    private List< TupleOfCar > tupleOfCarList = new ArrayList<>();
    private List< Patrul > patrulList = new ArrayList<>();
}
