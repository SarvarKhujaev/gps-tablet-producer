package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.datastax.driver.core.Row;

import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class PatrulActivityStatistics {
    private Patrul patrul;
    private final List< Long > dateList = new ArrayList<>();

    public PatrulActivityStatistics ( Patrul patrul, Flux< Row > rowFlux ) {
        this.setPatrul( patrul );
        rowFlux.filter( row -> row.getString( "status" ).equals( Status.LOGOUT ) )
                .subscribe( row -> this.getDateList().add( row.getLong( "totalActivityTime" ) ) ); }
}
