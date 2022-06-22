package com.ssd.mvd.gpstracker.response;

import com.datastax.driver.core.Row;
import com.ssd.mvd.gpstracker.entity.Patrul;
import lombok.Data;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Data
public class PatrulActivityStatistics {
    private Patrul patrul;
    private final List< Long > dateList = new ArrayList<>();

    public PatrulActivityStatistics ( Patrul patrul, Flux< Row > rowFlux ) {
        this.setPatrul( patrul );
        rowFlux.subscribe( row -> this.getDateList().add( row.getLong( "totalActivityTime" ) ) ); }
}
