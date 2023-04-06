package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

@lombok.Data
public class PatrulStatus {
    private Patrul patrul;
    private Boolean inTime; // показывает пришел ли Патрульный вовремя
    private Long totalTimeConsumption; // показывает сколько времени Патрульный потратил на всю задачу от начала до конца

    public PatrulStatus ( Patrul patrul ) {
        this.setPatrul( patrul );
        this.setInTime( patrul.check() );
        this.setTotalTimeConsumption( TimeInspector
                .getInspector()
                .getGetTimeDifferenceInSeconds()
                .apply( patrul.getTaskDate().toInstant() ) ); }
}
