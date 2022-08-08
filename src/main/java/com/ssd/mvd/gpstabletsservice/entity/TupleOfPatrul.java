package com.ssd.mvd.gpstabletsservice.entity;

import com.ssd.mvd.gpstabletsservice.constants.Countries;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.datastax.driver.core.Row;
import java.util.List;
import lombok.Data;

@Data
public class TupleOfPatrul {
    private Polygon polygon;
    private Countries countries;
    private List< ReqCar > reqCarsList;

    public TupleOfPatrul ( Row row ) {
        this.setReqCarsList( row.getList( "carList", ReqCar.class ) );
        this.setCountries( Countries.valueOf( row.getString( "country" ) ) );
        this.setPolygon( SerDes.getSerDes().deserializePolygon( row.getString( "object" ) ) ); }
}
